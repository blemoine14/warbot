package myteam;

import edu.warbot.agents.agents.WarBase;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarBaseBrain;
import edu.warbot.communications.WarMessage;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public abstract class WarBaseBrainController extends WarBaseBrain implements IntWarAgentConfig{

    private boolean _inDanger;
    private Map<WarAgentType,Integer> effectifs;
    private WTask ctask;
    private Stack<WTask> stack;

    public WarBaseBrainController() {
        super();
        this.ctask = this.countEffectifs;
        this.effectifs = new HashMap<>();
        this.effectifs.put(WarAgentType.WarExplorer,0);
        this.effectifs.put(WarAgentType.WarEngineer,0);
        this.effectifs.put(WarAgentType.WarLight,0);
        this.effectifs.put(WarAgentType.WarHeavy,0);
        this.effectifs.put(WarAgentType.WarKamikaze,0);
        this.effectifs.put(WarAgentType.WarTurret,0);
        this.effectifs.put(WarAgentType.WarRocketLauncher,0);
        _inDanger = false;
    }

    static WTask countEffectifs = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarBaseBrainController me = (WarBaseBrainController) bc;
            me.resetEffectifs();
            me.broadcastMessageToAll(WarUtilMessage.ASK_REPORT,"");
            me.ctask = me.waitingReport;
            return ACTION_IDLE;
        }
    };
    
    static WTask waitingReport = new WTask(){
        private int timeWaited = 0;
        private int timeToWait = 5;
        @Override
        String exec(WarBrain bc) {
            WarBaseBrainController me = (WarBaseBrainController) bc;
            if(this.timeToWait < this.timeWaited){
                for(WarMessage m : me.getMessages()){
                    if(m.getMessage().equals(WarUtilMessage.REPORTING)){
                        me.effectifs.replace(m.getSenderType(), me.effectifs.get(m.getSenderType())+1);
                    }
                }
            }
            me.ctask = me.balanceArmy;
            return ACTION_IDLE;
        }
    };
    
    static WTask balanceArmy = new WTask(){
        @Override
        String exec(WarBrain bc) {
            WarBaseBrainController me = (WarBaseBrainController) bc;
            if(me.getHealth() < 2500){
                return ACTION_IDLE;
            }
            else{
                int totEff = me.effectifTotal();
                if(me.effectifs.get(WarAgentType.WarExplorer) < 3){
                    if(totEff > 10){
                        me.setNextAgentToCreate(WarAgentType.WarExplorer);
                    }
                }
                else{
                    //faire deux modes à chaques agent :
                    //explorer : platoonLeader/recolter
                    //soldier : inGroup (desactive fuite)
                    
                    //finir transmission information avec la position indirect (if ingroup transmet to platoon leader) else transmet to base
                    
                    //installer des defences autours de la bases 1/2 tourelles avec des requests (retest give par base)
                    //emergency of base (compter nombre de base)
                }
            }
            return ACTION_IDLE;
        }
    };

    @Override
    public String action() {

        List<WarMessage> messages = getMessages();

        for (WarMessage message : messages) {
            if (message.getMessage().equals(WarUtilMessage.SEARCHING_BASE))
                reply(message, WarUtilMessage.BASE_FOUND);
        }

        List<WarAgentPercept> enemiesPercepts = this.getPerceptsEnemies();
        for (WarAgentPercept percept : enemiesPercepts) {
            if (isEnemy(percept) && percept.getType().getCategory().equals(WarAgentCategory.Soldier))
                broadcastMessageToAll(WarUtilMessage.BASE_UNDER_ATTACK,WarUtilMessage.serializeCoord(percept));
        }
        if(enemiesPercepts.size() > 5){
            
        }

        for (WarAgentPercept percept : getPerceptsResources()) {
            if (percept.getType().equals(WarAgentType.WarFood))
                broadcastMessageToAgentType(WarAgentType.WarExplorer, WarUtilMessage.FOOD_FOUND,
                        WarUtilMessage.serializeCoord(percept));
        }

        return WarBase.ACTION_IDLE;
    }
    
    @Override
    public WarAgentType getType() {
        return WarAgentType.WarBase;
    }
    
    private void resetEffectifs(){
        this.effectifs.replace(WarAgentType.WarExplorer,0);
        this.effectifs.replace(WarAgentType.WarEngineer,0);
        this.effectifs.replace(WarAgentType.WarLight,0);
        this.effectifs.replace(WarAgentType.WarHeavy,0);
        this.effectifs.replace(WarAgentType.WarKamikaze,0);
        this.effectifs.replace(WarAgentType.WarTurret,0);
        this.effectifs.replace(WarAgentType.WarRocketLauncher,0);
    }
    
    private int effectifTotal(){
        int sum = 0;
        sum+=this.effectifs.get(WarAgentType.WarExplorer);
        sum+=this.effectifs.get(WarAgentType.WarEngineer);
        sum+=this.effectifs.get(WarAgentType.WarLight);
        sum+=this.effectifs.get(WarAgentType.WarHeavy);
        sum+=this.effectifs.get(WarAgentType.WarKamikaze);
        sum+=this.effectifs.get(WarAgentType.WarTurret);
        sum+=this.effectifs.get(WarAgentType.WarRocketLauncher);
        return sum;
    }
}
