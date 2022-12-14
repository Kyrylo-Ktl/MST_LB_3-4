import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.aima.core.environment.wumpusworld.AgentPosition;
import main.aima.core.environment.wumpusworld.HybridWumpusAgent;
import main.aima.core.environment.wumpusworld.WumpusAction;
import main.aima.core.environment.wumpusworld.WumpusPercept;

import java.util.Optional;

public class NavigatorAgent extends jade.core.Agent {

//    /*

    //
//    Агент-навигатор, имеет 2 поведения -
//    циклическое прослушивающее почтовый ящик на предмет сообщений от спелиолога
//    и одноразовое поведение - делающее грамматический разбор (с помощью регулярных выражений),
//    запускающее алгоритм принятия решений (см. соответствующее решение на https://github.com/aimacode)
//    и формирующее естественно-языковое сообщение с описанием следующего действия,
//    которое отправляется в ответ навигатору.
//     */

    private static HybridWumpusAgent h_agent = null;
    private boolean is_busy = false;

    protected void setup() {
        System.out.println(getAID().getName() + ": GM online B)");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("navigator");
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
        System.out.println(getAID().getName() + ": It gets boring there..");
    }

    public static HybridWumpusAgent getHybridAgent() {
        if (h_agent == null) {
            h_agent = new HybridWumpusAgent(4, 4, new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH));
        }
        return h_agent;
    }

    private class FindGamer extends Behaviour {
        private boolean done;

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                if (!is_busy) {// FIXME
                    h_agent = getHybridAgent();
                    is_busy = true;
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(getAID().getName() + ": 50% of loot and I'll help you");
                    done = true;
                    addBehaviour(new EmailServer());
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent(getAID().getName() + ": you are noob!");
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
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    WumpusPercept percept = PerceptDictionary.getPercept(msg.getContent());
                    Optional<WumpusAction> action = h_agent.act(percept);
                    String action_msg = ActionDictionary.getSentence(action.get());
                    System.out.println(getAID().getName() + ":  " + action_msg);
                    reply.setContent(action_msg);
                    reply.setPerformative(ACLMessage.PROPOSE);
                } else if (msg.getPerformative() == ACLMessage.CANCEL) {
                    System.out.println(getAID().getName() + ": Yee. Mail me the money.");
                    doDelete();
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
