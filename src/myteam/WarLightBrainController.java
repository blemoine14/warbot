package myteam;

import edu.warbot.agents.agents.WarHeavy;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarLightBrain;
import edu.warbot.tools.geometry.PolarCoordinates;
import java.util.Stack;

public abstract class WarLightBrainController extends  WarLightBrain implements IntWarAgentConfig {
    private Stack<WTask> stack;
    private WTask ctask;
    private double angleToTarget;
    private int timeToDodge;
    private int timeDodged;
    private int targetId;
    private int targetPriority;


    public WarLightBrainController() {
        super();
        this.stack = new Stack<>();
        this.ctask = this.explore;
        this.targetPriority = 0;
    }
    
    WTask attackTarget = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarLightBrainController me = (WarLightBrainController) bc;
            for (WarAgentPercept wp : getPerceptsEnemies()) {
                //si ennemi dans le champ de vision on engage le combat
                if(wp.getID() == me.targetId){
                    //me.anticipateDisplacement(wp);
                    me.setHeading(wp.getAngle());
                    me.setDebugString("Attaque target");
                    if (isReloaded()){
                        return ACTION_FIRE;
                    }
                    else{
                        return ACTION_RELOAD;
                    }
                }
            }
            //si on ne trouve pas l'ennemi
            if(me.angleToTarget == me.getHeading()){ //si regarde dans sa dernière position connu on abandonne l'attaque
                me.targetPriority = 0;
                me.ctask = explore;
                return ACTION_MOVE;
            }
            else{ //sinon on regarde dans la dernière position connue
                setHeading(me.angleToTarget);
                return ACTION_IDLE;
            }
        }
        
    };
    
    WTask dodge = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarLightBrainController me = (WarLightBrainController) bc;
            me.setDebugString("Dodging "+me.timeDodged);
            if(!(me.timeDodged < me.timeToDodge) || me.isReloaded()){
                me.ctask = me.stack.pop();
                setHeading(me.angleToTarget);
                me.timeDodged = 0;
                return ACTION_IDLE;
            }
            else{
                if(me.isBlocked()){
                    me.setHeading(me.getHeading()+180);
                }
                me.timeDodged++;
                return ACTION_MOVE;
            }
            
        }
        
    };
    
    WTask explore = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarLightBrainController me = (WarLightBrainController) bc;
            me.setDebugString("Exploring");
            me.angleToTarget = me.getHeading();
            if(!(me.getTargetHealth() < 20*2)){
                for (WarAgentPercept wp : me.getPerceptsEnemies()) {
                    int priority = me.prioriteFocus(wp);
                    if(me.targetPriority < priority){
                        me.ctask = me.attackTarget;
                        me.targetId = wp.getID();
                        me.targetPriority = priority;
                    }
                }
            }
            if(me.isBlocked()){
                me.setRandomHeading();
            }
            return ACTION_MOVE;
        }
        
    };

    @Override
    public String action() {
        String toReturn = this.reflexe();
        if(toReturn != null){
            return toReturn;
        }
        this.setDebugString(this.targetPriority+" "+this.timeDodged+"/"+this.timeToDodge);
        toReturn = ctask.exec(this);   // le run de la FSM

        if(toReturn == null){
            if (isBlocked())
                this.setRandomHeading();
            return this.move();
        } else {
            return toReturn;
        }
    }
    
    
    private String reflexe(){
        for(WarAgentPercept wp : this.getPerceptsEnemies()){
            if(this.escape(wp)){
                return ACTION_MOVE;
            }
            if(wp.getType().getCategory().equals(WarAgentCategory.Projectile) && !this.ctask.equals(this.dodge)){
                this.angleToTarget = this.getHeading();
                this.timeToDodge = (int)Math.floor((float)WarUtils.getExplosionRadius(wp.getType())+(float)WarUtils.getRadius(this.getType()));
                if((wp.getDistance() > WarUtils.getSpeed(wp.getType())*(this.timeToDodge)) && (Math.abs(WarUtils.angleIncomingPercept(wp)) < 5)){
                    this.setHeading(wp.getHeading()+90);
                    this.timeDodged = 0;
                    this.stack.add(this.ctask);
                    this.ctask = this.dodge;
                    return ACTION_MOVE;
                }
            }
        }
        return null;
    }
    
    private int prioriteFocus(WarAgentPercept wp){
        switch(wp.getType()){ //gestion de l'ordre de priorité
            case Wall :
                return 1;
            case WarExplorer :
                return 2;
            case WarEngineer :
                return 3;
            case WarBase :
                return 4;
            case WarTurret :
                return 5;
            case WarLight :
                return 6;
            case WarRocketLauncher :
                return 7;
            default :
                return 0;
        }
    }
    
    private boolean escape(WarAgentPercept wp){
        switch(wp.getType()){
            case WarHeavy :
                if(Math.abs(WarUtils.angleIncomingPercept(wp)) < WarHeavy.ANGLE_OF_VIEW/2){
                    this.setHeading(wp.getHeading()+90);
                    return true;
                }
            case WarShell :
                if(Math.abs(WarUtils.angleIncomingPercept(wp)) < 5){
                    this.setHeading(wp.getHeading()+90);
                    return true;
                }
            case WarKamikaze :
                return true;
            default :
                return false;
        }
    }
    
    private int getTargetHealth(){
        for(WarAgentPercept wp : this.getPercepts()){
            if(wp.getID()==this.targetId){
                return wp.getHealth();
            }
        }
        return 100;
    }
    
    private void anticipateDisplacement(WarAgentPercept p){
        PolarCoordinates pc = WarUtils.addPolars(p.getDistance(),p.getAngle(),WarUtils.getSpeed(p.getType())*(p.getDistance()/WarUtils.getSpeed(WarAgentType.WarBullet)+1),p.getHeading());
        setHeading(pc.getAngle());
    }
    
    
    
    @Override
    public WarAgentType getType() {
        return WarAgentType.WarLight;
    }

}