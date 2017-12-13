package myteam;

import static edu.warbot.agents.actions.constants.AgressiveActions.ACTION_FIRE;
import static edu.warbot.agents.actions.constants.AgressiveActions.ACTION_RELOAD;
import static edu.warbot.agents.actions.constants.IdlerActions.ACTION_IDLE;
import static edu.warbot.agents.actions.constants.MovableActions.ACTION_MOVE;
import edu.warbot.agents.agents.WarHeavy;
import edu.warbot.agents.agents.WarRocketLauncher;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarRocketLauncherBrain;
import edu.warbot.tools.geometry.PolarCoordinates;
import java.util.Stack;

public abstract class WarRocketLauncherBrainController extends WarRocketLauncherBrain implements IntWarAgentConfig {
private Stack<WTask> stack;
    private WTask ctask;
    private double angleToTarget;
    private int timeToDodge;
    private int timeDodged;
    private int targetId;
    private int targetPriority;


    public WarRocketLauncherBrainController() {
        super();
        this.stack = new Stack<>();
        this.ctask = this.explore;
        this.targetPriority = 0;
    }
    
    WTask attackTarget = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
            for (WarAgentPercept wp : getPerceptsEnemies()) {
                //si ennemi dans le champ de vision on engage le combat
                if(wp.getID() == me.targetId){
                    me.setHeading(wp.getAngle());
                    me.setTargetDistance(wp.getDistance());
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
            WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
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
            WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
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
            case WarLight :
                return 4;
            case WarRocketLauncher :
                return 5;
            case WarHeavy :
                return 6;
            case WarBase :
                return 7;
            case WarTurret :
                return 8;
            default :
                return 0;
        }
    }
    
    private boolean escape(WarAgentPercept wp){
        return false;
    }
    
    private int getTargetHealth(){
        for(WarAgentPercept wp : this.getPercepts()){
            if(wp.getID()==this.targetId){
                return wp.getHealth();
            }
        }
        return 100;
    }
    
    @Override
    public WarAgentType getType(){
        return WarAgentType.WarRocketLauncher;
    }

}