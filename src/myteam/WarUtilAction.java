/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myteam;

import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import java.util.List;
import java.util.Random;

/**
 *
 * @author blemoine02
 */
public class WarUtilAction {
    public static WarAgentPercept getperceptFood(WarBrain bc){
        List<WarAgentPercept> percepts = bc.getPercepts();
        if(percepts.isEmpty()){
            return null;
        }
        else{
            Double dMin = null;
            WarAgentPercept res = null;
            for (WarAgentPercept percept : percepts){
                if(percept.getType() == WarAgentType.WarFood){
                    if(dMin == null || dMin > percept.getDistance()){
                        dMin = percept.getDistance();
                        res = percept;
                    }
                }
            }
            return res;   
        }
    }
    
    public static void wiggle(WarBrain bc){
        Random randomGenerator = new Random();
        bc.setHeading(randomGenerator.nextInt(20));
    }
}
