import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.aima.core.environment.wumpusworld.WumpusAction;

public class SpeleologistAgent extends jade.core.Agent {
    /*
    Спелиолог может отправлять к среде сообщения двух типов:
    REQUEST - запрос на описание текущего состояния среды в месте
    где сейчас он находится и CFP - предложение на выполнение действия.

По первому типу сообщений, агент-среда формирует ответ в виде предиката
Percept([stench, breeze, glitch, scream, bump, scream], временной маркер).
Второй тип сообщений передает среде сообщение Action(название действия),
который заставляет среду изменить свое состояние (положение или направление агента,
перемещение объекта-золото в рюкзак агента, смерть вампуса)
и ответить агенту-спелиологу сообщением типа ACCEPT с телом ОК.
     */
    /*
    Агент-спелиолог, общается с Средой и Навигатором.
    В начале спелиолог находит себе пещеру и навигатора (например из желтых страниц).
     */
    /*
    Так же у него должно быть реализовано сложное (Generic) поведение состоящее из нескольких шагов:

    Сформировать запрос среде типа REQUEST (см. выше)
    Получить ответ от среды с описанием состояния в виде кортежа или предиката
    Подготовить сообшение навигатору на естественном языке,
    которое включает в себя все полученные свойства.
    Для этого нужно реализовать набор синонимичных английских предложений (словарь)
    и случайным образом выбирать одно из них по соответствующему свойству:
    например если среда сообщила что в комнате есть сквозняк, агент должен
     выбрать из словаря одно из сообщений: ‘I feel breeze here’, ‘There is a breeze’, ‘It’s a cool breeze here’.
     Если присутствуют больше чем один признак, то все предложения
     нужно соединить в одно текстовое сообщение, разделенные точкой.
    Отправить подготовленное сообщение навигатору
    Ожидать получение и получить ответ о навигатора с текстовым
    сообщением о предполагаемом действии. Выполнить простейший грамматический
    разбор, чтобы определить одно из 6 доступных действий: Turn(left), Turn(right), Forward, Shoot, Grab, or Climb.
    Сформировать сообщение агенту-среде типа CFP (см. выше) и отправить его.
    Ожидать ответа от среды. В случае если последнее выполненное действие
    было climb то завершить решение задачи (успехом если принесено золото или неуспехом если нет решения)
    и выполнить команды doDelete() для всех агентов. В противном случае перейти к шагу 1.
     */


    private AID[] dungAgents;
    private AID[] navigatorAgents;
    private AID myDung = null;
    private AID myNavigator = null;

    private WumpusAction lastAction;

    private TickerBehaviour requestDF = new TickerBehaviour(this, 15000) {
        protected void onTick() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("dungeon");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                dungAgents = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    dungAgents[i] = result[i].getName();
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
            sd.setType("navigator");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                navigatorAgents = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    navigatorAgents[i] = result[i].getName();
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    };


    protected void setup() {
        System.out.println(getAID().getName() + ": Hi!");

        addBehaviour(requestDF);
        addBehaviour(new PartyToDung());
    }

    protected void takeDown() {
        super.takeDown();
        System.out.println(getAID().getName() + ": I'm off");
    }


    private class PartyToDung extends Behaviour {
        private MessageTemplate mt;
        private int step = 0;
        private boolean dungOk = false, navOk = false;

        public void action() {
            if ((dungAgents == null || navigatorAgents == null) ||
                    (dungAgents.length == 0 || navigatorAgents.length == 0))
                return;

            switch (step) {
                default:
                    break;
                case 0:
                    System.out.println(getAID().getName() + ": When does the event start?");
                    System.out.println(getAID().getName() + ": hey, who wish to join my group?");

                    myDung = dungAgents[0];
                    myNavigator = navigatorAgents[0];
                    ACLMessage event = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    event.addReceiver(myDung);
                    event.addReceiver(myNavigator);
                    event.setConversationId("start-game");
                    event.setReplyWith("event" + System.currentTimeMillis());
                    myAgent.send(event);
                    // Prepare the template to get the reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("start-game"),
                            MessageTemplate.MatchInReplyTo(event.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            AID sender = reply.getSender();
                            if (myDung.getName().equals(sender.getName())) {
                                System.out.println(getAID().getName() + ": I'm going to kill Wumpus!!");
                                dungOk = true;
                            }
                            if (myNavigator.getName().equals(sender.getName())) {
                                System.out.println(getAID().getName() + ": half of my gold?!");
                                System.out.println(getAID().getName() + ": ok =/");
                                navOk = true;
                            }
                            if (dungOk && navOk) {
                                step = 2;
                                requestDF.stop();
                                addBehaviour(new JustFollowOrders());
                                System.out.println(getAID().getName() + ": WOW.. It's my first time in a dungeon!");
                            }
                        } else if (reply.getPerformative() == ACLMessage.FAILURE) {
                            System.out.println(getAID().getName() + ": As always..(");
                            if (myDung == reply.getSender() && dungAgents.length > 1) {
                                myDung = dungAgents[1];
                            }
                            if (myNavigator == reply.getSender() && navigatorAgents.length > 1) {
                                myNavigator = navigatorAgents[1];
                            }
                            step = 0;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            return (step == 2);
        }
    }

    private class JustFollowOrders extends CyclicBehaviour {
        private MessageTemplate mt;
        private int step = 0;

        public void action() {

            switch (step) {
                default:
                    break;
                case 0:
                    ACLMessage where = new ACLMessage(ACLMessage.REQUEST);
                    where.addReceiver(myDung);
                    where.setConversationId("position");
                    where.setReplyWith("position" + System.currentTimeMillis());
                    myAgent.send(where);
                    // Prepare the template to get the reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("position"),
                            MessageTemplate.MatchInReplyTo(where.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            String envInfo = reply.getContent();
                            String natlangmsg = PerceptDictionary.getSentence(envInfo);
                            System.out.println(getAID().getName() + ": " + natlangmsg);

                            ACLMessage percept = new ACLMessage(ACLMessage.INFORM);
                            percept.addReceiver(myNavigator);
                            percept.setContent(natlangmsg);
                            percept.setConversationId("position");
                            percept.setReplyWith("position" + System.currentTimeMillis());
                            myAgent.send(percept);
                            // Prepare the template to get the reply
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("position"),
                                    MessageTemplate.MatchInReplyTo(percept.getReplyWith()));
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            String action = reply.getContent();
                            lastAction = ActionDictionary.getAction(action);

                            ACLMessage doIt = new ACLMessage(ACLMessage.CFP);
                            doIt.addReceiver(myDung);
                            doIt.setContent(lastAction.toString());
                            doIt.setConversationId("act");
                            doIt.setReplyWith("act" + System.currentTimeMillis());
                            myAgent.send(doIt);
                            // Prepare the template to get the reply
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("act"),
                                    MessageTemplate.MatchInReplyTo(doIt.getReplyWith()));
                            step = 3;
                        }
                    } else {
                        block();
                    }
                    break;
                case 3:
                    /*
                    Ожидать ответа от среды. В случае если последнее выполненное
                    действие было climb то завершить решение задачи
                    (успехом если принесено золото или неуспехом если нет решения)
                    и выполнить команды doDelete() для всех агентов. В противном случае перейти к шагу 1.
                     */
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            if (lastAction == WumpusAction.CLIMB) {
                                System.out.println(getAID().getName() + ": Yippee! We did it! Thanks!");
                                ACLMessage poison = new ACLMessage(ACLMessage.CANCEL);
                                poison.addReceiver(myNavigator);
                                poison.addReceiver(myDung);
                                myAgent.send(poison);
                                doDelete();
                            }
                            step = 0;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }
    }

}
