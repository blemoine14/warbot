package myteam;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarResource;
import static edu.warbot.agents.actions.constants.ControllableActions.ACTION_EAT;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarEngineerBrain;
import edu.warbot.communications.WarMessage;
import java.util.ArrayList;
import java.util.List;

public abstract class WarEngineerBrainController extends WarEngineerBrain {
    
    WTask ctask;
    int timeout = 5;
    int TimeWaited = 0;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeWaited() {
        return TimeWaited;
    }

    public void setTimeWaited(int timeTimeWaiteded) {
        this.TimeWaited = TimeWaited;
    }
    
    

    static WTask handleMsgs = new WTask(){ 
            String exec(WarBrain bc){return "";}
    };
    
    static WTask buildTurret = new WTask(){ 
        String exec(WarBrain bc){
            WarEngineerBrainController me = (WarEngineerBrainController) bc;
            
            //me.setDebugString("Building turret");
            
            List<WarAgentPercept> percepts = me.getPerceptsAlliesByType(WarAgentType.WarTurret);
            if(percepts.isEmpty()){
                me.setNextBuildingToBuild(WarAgentType.WarTurret);
                me.ctask = searchHelp;
                return me.build();
            }
            else{
                WarUtilAction.wiggle(me);
                return me.move();
            }
        }
    };
    
    static WTask searchHelp = new WTask(){
        boolean proposalSend = false;
        boolean proposalAccept = false;
            
        String exec(WarBrain bc){
            WarEngineerBrainController me = (WarEngineerBrainController) bc;
            
            
            
            if(proposalSend){
                me.setDebugString("search someone ");
            }
            if(proposalAccept){
                me.setDebugString("found someone");
            }
            
            
            if(!proposalAccept){
                if(me.TimeWaited < me.timeout){
                    if(!proposalSend){
                        me.broadcastMessageToAgentType(WarAgentType.WarExplorer, WarUtilMessage.NEED_SOMEONE, WarUtilMessage.NEED_HEALTH);
                        proposalSend = true;
                    }
                    else{
                        List<WarMessage> messages = me.getMessages();
                        Double dMin = null;
                        WarMessage explNear = null;
                        List<WarMessage> answers = new ArrayList<>();
                        for(WarMessage message : messages){
                            if(message.getMessage().equals(WarUtilMessage.DISPO)){
                                answers.add(message);
                                if(dMin == null || dMin > message.getDistance()){
                                    dMin = message.getDistance();
                                    explNear = message;
                                }
                            }
                        }
                        if(explNear == null){
                            //me.setDebugString("found no one");
                            me.TimeWaited++;

                        }
                        else{
                            //me.setDebugString("found someone");
                            answers.remove(explNear);
                            me.reply(explNear,WarUtilMessage.CHOOSE_YOU , WarUtilMessage.NEED_HEALTH);
                            for(WarMessage message : answers){
                                me.reply(message,WarUtilMessage.NOT_CHOOSE_YOU , "");
                            }
                            me.TimeWaited = 0;
                            proposalAccept = true;
                        }
                    }
                }
                else{
                    me.TimeWaited = 0;
                    proposalSend = false;
                    proposalAccept = false;
                }
            }
            
            
            
            if(me.getHealth() > me.getMaxHealth() * 0.7){
                me.ctask = getFoodTask;
                return(null);
            }

            //me.setDebugStringColor(Color.green.darker());
            //me.setDebugString("Go to safety");

            List<WarAgentPercept> basePercepts = me.getPerceptsAlliesByType(WarAgentType.WarBase);

            //Si je ne vois pas de base
            if(basePercepts == null | basePercepts.size() == 0){

                WarMessage m = me.getMessageFromBase();
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
    
    static WTask getFoodTask = new WTask(){
        String exec(WarBrain bc){
            WarEngineerBrainController me = (WarEngineerBrainController) bc;

            //me.setDebugStringColor(Color.BLACK);
            //me.setDebugString("Searching food");

            WarAgentPercept foodPercept = WarUtilAction.getperceptFood(me);

            //Si il y a de la nouriture
            if(foodPercept != null){
                if(foodPercept.getDistance() > WarResource.MAX_DISTANCE_TAKE){
                        me.setHeading(foodPercept.getAngle());
                }else{
                        me.ctask = buildTurret;
                        return me.take();
                }
            }
            else{
                WarMessage m = me.getMessageAboutFood();
                if(m != null){
                    me.setHeading(m.getAngle());
                }
            }
            if(me.isBlocked())
                me.setRandomHeading();
            
            return me.move();
        }
    };

    public WarEngineerBrainController() {
        super();
        this.ctask = getFoodTask;
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
        if (this.getHealth() < this.getMaxHealth() * 0.5){
            if(this.getNbElementsInBag()>0){
                return ACTION_EAT;
            }
            else{
                this.ctask = searchHelp;
            }
        }
        return null;
    }


    private WarMessage getMessageAboutFood() {
        for (WarMessage m : getMessages()) {
            if(m.getMessage().equals(WarUtilMessage.FOOD_FOUND))
                return m;
        }
        return null;
    }
    
    private WarMessage getMessageFromBase() {
        for (WarMessage m : getMessages()) {
            if(m.getSenderType().equals(WarAgentType.WarBase))
                return m;
        }

        broadcastMessageToAgentType(WarAgentType.WarBase, WarUtilMessage.SEARCHING_BASE, "");
        return null;
    }
}
