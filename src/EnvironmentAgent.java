import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.aima.core.environment.wumpusworld.HybridWumpusAgent;
import main.aima.core.environment.wumpusworld.WumpusAction;
import main.aima.core.environment.wumpusworld.WumpusCave;
import main.aima.core.environment.wumpusworld.WumpusEnvironment;

public class EnvironmentAgent extends jade.core.Agent {

    WumpusEnvironment environment;
    HybridWumpusAgent agent = null;

    /*
    Среда должна в себе содержать поля соответствующие:
    полю лабиринта (матрица 4 на 4);
    список объектов (ямы, вампус, золото);
    список агентов (ссылка на агента спелиолога, который опустился в эту пещеру и его координаты
    и направление куда смотрит - юг, север, восток и запад); текущий такт времени.
     */
    /*
    У агента-Среда, должна быть реализована регистрация в DF (агенте желтых страниц) услуги:
    Прогулка по пещере сокровищ, а так же минимум 2 поведения (одно
    циклическое для обслуживание почтового ящика и
    второе одноразовое для формирования ответа на запрос агента-спелиолога).
     */
    /*
    Спелиолог может отправлять к среде сообщения двух типов: REQUEST - запрос на описание текущего
    состояния среды в месте где сейчас он находится и
    CFP - предложение на выполнение действия.

    По первому типу сообщений, агент-среда формирует ответ в виде предиката
    Percept([stench, breeze, glitch, scream, bump, scream], временной маркер).
    Второй тип сообщений передает среде сообщение Action(название действия),
    который заставляет среду изменить свое состояние (положение или направление агента,
    перемещение объекта-золото в рюкзак агента, смерть вампуса)
    и ответить агенту-спелиологу сообщением типа ACCEPT с телом ОК.
     */

    private static WumpusCave create2x2Cave() {
        // Caution: 2x2 caves need a wumpus - otherwise KB becomes inconsistent in step 2...
        return new WumpusCave(2, 2, ""
                + "W . "
                + "S G ");
    }

    private static WumpusCave create3x3Cave() {
        return new WumpusCave(3, 3, ""
                + "P . G "
                + ". W . "
                + "S . P ");
    }

    /**
     * Artificial Intelligence A Modern Approach (3rd Edition): page 237.<br>
     * <br>
     * Figure 7.2 A typical wumpus world. The agent is in the bottom left corner, facing right.
     */
    private static WumpusCave create4x4Cave() {
        return new WumpusCave(4, 4, ""
                + ". . . P "
                + "W G P . "
                + ". . . . "
                + "S . P . ");
    }

    protected void setup() {
        environment = new WumpusEnvironment(create4x4Cave());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("dungeon");
        sd.setName("JADE-wumpus-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new FindGamer());
    }

    protected void takeDown() {
        super.takeDown();
        System.out.println("Event finished");
    }

    private class FindGamer extends Behaviour {
        private boolean done;

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                if (agent == null) {
                    agent = NavigatorAgent.getHybridAgent();
                    environment.addAgent(agent);
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println("Gamers! Wumpus return to our world!");
                    done = true;
                    addBehaviour(new EmailServer());
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("sorry, event is postponed");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }

        public boolean done() {
            return done;
        }
    }

    private class EmailServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(environment.getPerceptSeenBy(agent).toString());
                    System.out.println("Party1 on position: " + environment.getAgentPosition(agent));
                } else if (msg.getPerformative() == ACLMessage.CFP) {
                    String action = msg.getContent();
                    WumpusAction act = WumpusAction.valueOf(action);
                    environment.execute(agent, act);
                    System.out.println("game logs: " + action);
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    reply.setContent("OK");
                } else if (msg.getPerformative() == ACLMessage.CANCEL) {
                    System.out.println(environment.getCave().toString());
                    doDelete();
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
