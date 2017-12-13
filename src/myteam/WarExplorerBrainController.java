package myteam;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarResource;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarExplorerBrain;
import edu.warbot.communications.WarMessage;
import edu.warbot.tools.geometry.PolarCoordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class WarExplorerBrainController extends WarExplorerBrain implements IntWarAgentConfig{
	
    boolean dispo = true;
    boolean waitAnswer = false;
    private int timeOut = 30;
    private int timeWaited = 0;
    private int targetId;
    WTask ctask;
    Stack<WTask> ptask = new Stack<>();

    static WTask handleMsgs = new WTask(){ 
            String exec(WarBrain bc){return "";}
    };
    
    static WTask giveFood = new WTask(){ 
        String exec(WarBrain bc){
            WarExplorerBrainController me = (WarExplorerBrainController) bc;
            
            if(me.timeWaited < me.timeOut){
                if(me.getNbElementsInBag() > 0){
                    me.setDebugString("helping someone");
                    me.sendMessage(me.targetId, WarUtilMessage.WHERE_ARE_YOU, "");
                    List<WarMessage> messages = me.getMessages();
                    boolean messageReceive = false;
                    for(WarMessage message : messages){
                        if(me.targetId == message.getSenderID()){
                            messageReceive = true;
                            if(message.getMessage().equals(WarUtilMessage.IM_HERE)){
                                me.timeWaited = 0;
                                if(message.getDistance() < MovableWarAgent.MAX_DISTANCE_GIVE){
                                    me.setIdNextAgentToGive(me.targetId);
                                    return me.give();
                                }
                                else{
                                    me.setHeading(message.getAngle());
                                }
                            }
                            if(message.getMessage().equals(WarUtilMessage.IM_FINE)){
                                me.ctask = me.ptask.pop();
                                me.dispo = true;
                            }
                        }
                    }
                    if(!messageReceive){
                        me.timeWaited++;
                    }
                }
                else{
                    me.sendMessage(me.targetId, WarUtilMessage.QUIT_ENROLMENT,"");
                    me.timeWaited = 0;
                    me.ctask = me.ptask.pop();
                    me.dispo = true;
                }
            }
            else{
                me.sendMessage(me.targetId, WarUtilMessage.QUIT_ENROLMENT,"");
                me.timeWaited = 0;
                me.ctask = me.ptask.pop();
                me.dispo = true;
            }
            
            if(me.isBlocked())
                me.setRandomHeading();

            return me.move();
        }
    };

    static WTask returnFoodTask = new WTask(){
        String exec(WarBrain bc){
            WarExplorerBrainController me = (WarExplorerBrainController) bc;
            if(me.isBagEmpty()){
                me.setHeading(me.getHeading() + 180);

                me.ctask = getFoodTask;
                return(null);
            }
            
            me.setDebugString("Returning Food");

            List<WarAgentPercept> basePercepts = me.getPerceptsAlliesByType(WarAgentType.WarBase);

            //Si je ne vois pas de base
            if(basePercepts == null | basePercepts.isEmpty()){

                WarMessage m = WarUtilAction.getMessageFromBase(me);
                //Si j'ai un message de la base je vais vers elle
                if(m != null)
                    me.setHeading(m.getAngle());
            }else{//si je vois une base
                WarAgentPercept base = basePercepts.get(0);

                if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
                    me.setHeading(base.getAngle());
                }else{
                    me.setIdNextAgentToGive(base.getID());
                    return me.give();
                }
            }
            
            if(me.isBlocked())
                me.setRandomHeading();
            
            return me.move();
        }
    };

    static WTask getFoodTask = new WTask(){
        String exec(WarBrain bc){
            WarExplorerBrainController me = (WarExplorerBrainController) bc;
            if(me.isBagFull()){
                me.ctask = returnFoodTask;
                return me.idle();
            }

            
            me.setDebugString("Searching food");

            WarAgentPercept foodPercept = WarUtilAction.getperceptFood(me);

            //Si il y a de la nouriture
            if(foodPercept != null){
                me.broadcastMessageToAll(WarUtilMessage.FOOD_FOUND, WarUtilMessage.serializeCoord(foodPercept));
                if(foodPercept.getDistance() > WarResource.MAX_DISTANCE_TAKE){
                        me.setHeading(foodPercept.getAngle());
                }else{
                        return me.take();
                }
            }
            else{
                PolarCoordinates v = WarUtilAction.getCoordFood(me);
                if(v != null){
                    me.setHeading(v.getAngle());
                }
            }
            if(me.isBlocked())
                me.setRandomHeading();
            
            return me.move();
        }
    };
    
    WTask searchHelp = new WTask(){
        boolean proposalSend = false;
        boolean proposalAccept = false;
        
        @Override
        String exec(WarBrain bc){
            WarExplorerBrainController me = (WarExplorerBrainController) bc;
            
            
            me.setDebugString("sending request : "+WarUtilMessage.NEED_HEALTH);
            if(proposalSend){
                me.setDebugString("search someone");
            }
            if(proposalAccept){
                me.setDebugString("found someone");
            }
            
            if(me.timeWaited < me.timeOut){
                if(!proposalAccept){
                    if(!proposalSend){
                        me.broadcastMessageToAll(WarUtilMessage.NEED_SOMEONE, WarUtilMessage.NEED_HEALTH);
                        me.timeWaited = 0;
                        proposalSend = true;
                    }
                    else{
                        List<WarMessage> messages = me.getMessages();
                        double dMin = 0;
                        WarMessage explNear = null;
                        List<WarMessage> answers = new ArrayList<>();
                        for(WarMessage message : messages){
                            if(message.getMessage().equals(WarUtilMessage.DISPO)){
                                answers.add(message);
                                if(explNear == null || dMin > message.getDistance()){
                                    dMin = message.getDistance();
                                    explNear = message;
                                }
                            }
                        }
                        if(explNear == null){
                            me.timeWaited++;

                        }
                        else{
                            answers.remove(explNear);
                            me.sendMessage(explNear.getSenderID(),WarUtilMessage.CHOOSE_YOU , WarUtilMessage.NEED_HEALTH);
                            for(WarMessage message : answers){
                                me.sendMessage(message.getSenderID(),WarUtilMessage.NOT_CHOOSE_YOU , "");
                            }
                            me.timeWaited = 0;
                            proposalAccept = true;
                        }
                    }
                }
                else{
                    boolean answer = false;
                    List<WarMessage> messages = me.getMessages();
                    for(WarMessage message : messages){
                        if(message.getMessage().equals(WarUtilMessage.WHERE_ARE_YOU)){
                            answer = true;
                            me.sendMessage(message.getSenderID(), WarUtilMessage.IM_HERE, "");
                            me.timeWaited = 0;
                        }
                        if(message.getMessage().equals(WarUtilMessage.QUIT_ENROLMENT)){
                            answer = true;
                            me.timeWaited = 0;
                            proposalSend = false;
                            proposalAccept = false;
                        }
                    }
                    if(!answer){
                        me.timeWaited++;
                    }
                }
            }
            else{
                me.timeWaited = 0;
                proposalSend = false;
                proposalAccept = false;
            }
            
            
            
            
            if(!(me.getHealth() <= me.getMaxHealth() * 0.2)){
                me.broadcastMessageToAll(WarUtilMessage.IM_FINE, "");
                me.setHeading(me.getHeading()+180);
                me.timeWaited = 0;
                proposalSend = false;
                proposalAccept = false;
                me.ctask = me.getFoodTask;
                return me.idle();
            }
            
            

            List<WarAgentPercept> basePercepts = me.getPerceptsAlliesByType(WarAgentType.WarBase);

            //Si je ne vois pas de base
            if(basePercepts == null | basePercepts.isEmpty()){

                WarMessage m = WarUtilAction.getMessageFromBase(me);
                //Si j'ai un message de la base je vais vers elle
                if(m != null)
                    me.setHeading(m.getAngle());
            }else{//si je vois une base
                WarAgentPercept base = basePercepts.get(0);

                if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
                    me.setHeading(base.getAngle());
                    return me.move();
                }else{
                    return me.idle();
                }
            }
            
            if(me.isBlocked())
                me.setRandomHeading();
            
            return me.move();
        }
    };



    public WarExplorerBrainController() {
        super();
        ctask = getFoodTask; // initialisation de la FSM
    }

@Override
    public String action() {

        // Develop behaviour here
        String toReturn = this.reflexe();
        if(toReturn != null){
            return toReturn;
        }

        toReturn = ctask.exec(this);   // le run de la FSM

        if(toReturn == null){
            if (isBlocked())
                    setRandomHeading();
            return this.move();
        } else {
            return toReturn;
        }
    }
    
    private String reflexe(){
        if (this.getHealth() <= this.getMaxHealth() * 0.2){
            if(this.getNbElementsInBag()>0){
                return ACTION_EAT;
            }
            else{
                this.ctask = this.searchHelp;
            }
        }
        List<WarMessage> messages = this.getMessages();
        
        
        for(WarMessage message : messages){
            if(dispo){
                this.respondToOffer(message);
            }
        }
        if(waitAnswer){
            if(this.timeWaited < this.timeOut){
                for(WarMessage message : messages){
                    this.beHired(message);
                }
            }
            else{
                this.timeWaited = 0;
                this.dispo = true;
                this.waitAnswer = false;
            }
        }
        return null;
    }


    
    private void respondToOffer(WarMessage message){
        if(message.getMessage().equals(WarUtilMessage.NEED_SOMEONE)){
            if(this.getNbElementsInBag()>0){
                this.reply(message, WarUtilMessage.DISPO, "");
                this.targetId = message.getSenderID();
                this.dispo = false;
                this.waitAnswer = true;
            }
        }
    }
    
    private void beHired(WarMessage message){
        if(message.getMessage().equals(WarUtilMessage.CHOOSE_YOU)){
            this.timeWaited = 0;
            this.waitAnswer=false;
            switch(message.getContent()[0]){
                case WarUtilMessage.NEED_HEALTH :
                    this.ptask.add(this.ctask);
                    this.ctask = giveFood;
                    this.targetId = message.getSenderID();
                    break;
                default :
                    break;
            }
        }
        if(message.getMessage().equals(WarUtilMessage.NOT_CHOOSE_YOU)){
            this.timeWaited = 0;
            this.dispo=true;
            this.waitAnswer=false;
        }
    }
    @Override
    public WarAgentType getType() {
        return WarAgentType.WarExplorer;
    }
}