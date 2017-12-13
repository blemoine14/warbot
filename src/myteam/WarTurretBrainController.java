package myteam;

import edu.warbot.agents.WarResource;
import edu.warbot.agents.agents.WarTurret;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.brains.WarTurretBrain;

import java.util.ArrayList;
import java.util.List;

public abstract class WarTurretBrainController extends WarTurretBrain implements IntWarAgentConfig {

    private int _sight;

    public WarTurretBrainController() {
        super();

        _sight = 0;
    }

    @Override
    public String action() {
        
         WarAgentPercept foodPercept = WarUtilAction.getperceptFood(this);

        //Si il y a de la nouriture
        if(foodPercept != null){
            this.broadcastMessageToAll(WarUtilMessage.FOOD_FOUND, WarUtilMessage.serializeCoord(foodPercept));
        }

        _sight += 90;
        if (_sight == 360) {
            _sight = 0;
        }
        setHeading(_sight);

        for (WarAgentPercept p : this.getPerceptsEnemies()) {
            if(!p.getType().getCategory().equals(WarAgentCategory.Resource)){
                this.setDebugString("Attaque");
                setHeading(p.getAngle());
                if (isReloaded()) {
                    return WarTurret.ACTION_FIRE;
                } else
                    return WarTurret.ACTION_RELOAD;
            }
        }

        return WarTurret.ACTION_IDLE;
    }

    @Override
    public WarAgentType getType() {
        return WarAgentType.WarTurret;
    }
}
