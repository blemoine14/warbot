package myteam;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarResource;
import static edu.warbot.agents.actions.constants.ControllableActions.ACTION_EAT;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarEngineerBrain;
import edu.warbot.communications.WarMessage;
import edu.warbot.tools.geometry.PolarCoordinates;
import java.util.ArrayList;
import java.util.List;

public abstract class WarEngineerBrainController extends WarEngineerBrain implements IntWarAgentConfig{
    
    WTask ctask;
    private int timeOut = 30;
    private int timeWaited = 0;
    
    

    WTask handleMsgs = new WTask(){ 
            @Override
            String exec(WarBrain bc){return "";}
    };
    
    WTask buildTurret = new WTask(){ 
        @Override
        String exec(WarBrain bc){
            WarEngineerBrainController me = (WarEngineerBrainController) bc;
            
            me.setDebugString("Building turret");
            
            List<WarAgentPercept> percepts = me.getPerceptsAlliesByType(WarAgentType.WarTurret);
            if(percepts.isEmpty()){
                me.setNextBuildingToBuild(WarAgentType.WarTurret);
                me.ctask = me.getFoodTask;
                return me.build();
            }
            else{
                WarUtilAction.wiggle(me);
                return me.move();
            }
        }
    };
    
    WTask askForHelp = new WTask(WarUtilMessage.NEED_HEALTH){
        @Override
        String exec(WarBrain bc){
            WarEngineerBrainController me = (WarEngineerBrainController) bc;
            
            me.setDebugString("sending request : "+this.message);
            
            me.broadcastMessageToAll(WarUtilMessage.NEED_SOMEONE, this.message);
            me.timeWaited = 0;
            me.ctask = me.waitAnswerToRequest;
            
            return me.returnToNearestBase();
        }
    };
    
    WTask waitAnswerToRequest = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarEngineerBrainController me = (WarEngineerBrainController) bc;
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
//                            me.setDebugString("found someone");
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
                //si personne ne repond
                if(explNear == null){
                    me.timeWaited++;
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
                    me.timeWaited = 0;
                    me.ctask = me.sendMyPosition;
                }
            }
            else{
                me.timeWaited = 0;
                me.ctask = me.askForHelp;
            }
            
            return me.returnToNearestBase();
        }
        
    };
    
     WTask sendMyPosition = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarEngineerBrainController me = (WarEngineerBrainController) bc;
            
            me.setDebugString("sending position");
            
            if(me.timeWaited < me.timeOut){
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
                        me.ctask = me.askForHelp;
                    }
                }
                if(!answer){
                    me.timeWaited++;
                }
            }
            else{
                me.timeWaited = 0;
                me.ctask = me.askForHelp;
            }
            
            if(!(me.getHealth() <= me.getMaxHealth() * 0.8)){
                me.broadcastMessageToAll(WarUtilMessage.IM_FINE, "");
                me.setHeading(me.getHeading()+180);
                me.timeWaited = 0;
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
            
            return me.returnToNearestBase();
        }
     };
            
    
    WTask getFoodTask = new WTask(){
        String exec(WarBrain bc){
            WarEngineerBrainController me = (WarEngineerBrainController) bc;

//            me.setDebugString("Searching food");

            WarAgentPercept foodPercept = WarUtilAction.getperceptFood(me);

            //Si il y a de la nouriture
            if(foodPercept != null){
                if(foodPercept.getDistance() > WarResource.MAX_DISTANCE_TAKE){
                        me.setHeading(foodPercept.getAngle());
                }else{
                        me.ctask = me.buildTurret;
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

    public WarEngineerBrainController() {
        super();
        this.ctask = this.getFoodTask;
    }

    @Override
    public String action() {
        
//        this.setDebugString(this.timeWaited+"/"+this.timeOut);


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
        if (this.getHealth() <= this.getMaxHealth() * 0.8){
            if(this.getNbElementsInBag()>0){
                return ACTION_EAT;
            }
            else{
                this.ctask = this.askForHelp;
            }
        }
        return null;
    }
    
    @Override
    public WarAgentType getType() {
        return WarAgentType.WarEngineer;

    }
}
