package myteam;

import static edu.warbot.agents.actions.constants.AgressiveActions.ACTION_FIRE;
import static edu.warbot.agents.actions.constants.AgressiveActions.ACTION_RELOAD;
import static edu.warbot.agents.actions.constants.IdlerActions.ACTION_IDLE;
import static edu.warbot.agents.actions.constants.MovableActions.ACTION_MOVE;
import edu.warbot.agents.agents.WarHeavy;
import edu.warbot.agents.agents.WarKamikaze;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarHeavyBrain;
import edu.warbot.tools.geometry.PolarCoordinates;
import java.util.Stack;

public abstract class WarHeavyBrainController extends  WarHeavyBrain implements IntWarAgentConfig{
    private Stack<WTask> stack;
    private WTask ctask;
    private double angleToTarget;
    private int timeToDodge;
    private int timeDodged;
    private int targetId;
    private int targetPriority;


    public WarHeavyBrainController() {
        super();
        this.stack = new Stack<>();
        this.ctask = this.explore;
        this.targetPriority = 0;
    }
    
    WTask attackTarget = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarHeavyBrainController me = (WarHeavyBrainController) bc;
            me.setDebugString("Attaque target");
            for (WarAgentPercept wp : getPerceptsEnemies()) {
                //si ennemi dans le champ de vision on engage le combat
                if(wp.getID() == me.targetId){
                    //me.anticipateDisplacement(wp);
                    me.setHeading(wp.getAngle());
                    if (isReloaded())
                        return ACTION_FIRE;
                    else
                        return ACTION_RELOAD;
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
    
    WTask explore = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarHeavyBrainController me = (WarHeavyBrainController) bc;
            me.setDebugString("Exploring");
            me.angleToTarget = me.getHeading();
            
            if(!(me.getTargetHealth() < 50*1)){
                for (WarAgentPercept wp : me.getPerceptsEnemies()) {
                    int priority = me.prioriteFocus(wp);
                    if(me.targetPriority < priority){
                        me.ctask = me.attackTarget;
                        me.targetId = wp.getID();
                        me.targetPriority = priority;
                    }
                }
                me.myRoles(ACTION_EAT);
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
            case WarHeavy :
                return 6;
            case WarLight :
                return 7;
            case WarRocketLauncher :
                return 8;
            default :
                return 0;
        }
    }
    
    private boolean escape(WarAgentPercept wp){
        switch(wp.getType()){
            case WarKamikaze :
                if(Math.abs(WarUtils.angleIncomingPercept(wp)) < WarKamikaze.ANGLE_OF_VIEW/2){
                    this.setHeading(wp.getHeading()+120);
                    return true;
                }
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
        return 5000;
    }
    
    private void anticipateDisplacement(WarAgentPercept p){
        PolarCoordinates pc = WarUtils.addPolars(p.getDistance(),p.getAngle(),WarUtils.getSpeed(p.getType())*(p.getDistance()/WarUtils.getSpeed(WarAgentType.WarShell)+1),p.getHeading());
        setHeading(pc.getAngle());
    }
}