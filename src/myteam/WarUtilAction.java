/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myteam;

import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.communications.WarMessage;
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
    
    public static Vector2 getCoordFood(WarBrain bc) {
        Vector2 food = null;
        for (WarMessage m : bc.getMessages()) {
            if(m.getMessage().equals(WarUtilMessage.FOOD_FOUND)){
                Vector2 vfood = getCoordPolaireSend(bc, m);
                if(food == null || food.x > vfood.x){
                    food = vfood;
                }
            }
        }
        return food;
    }

    public static WarMessage getMessageFromBase(WarBrain bc) {
        WarMessage base = null;
        for (WarMessage m : bc.getMessages()) {
            if(m.getSenderType().equals(WarAgentType.WarBase)){
                if(base == null || base.getDistance() > m.getDistance()){
                    base = m;
                }
            }
        }
        if(base == null){
            bc.broadcastMessageToAgentType(WarAgentType.WarBase, WarUtilMessage.SEARCHING_BASE, "");
        }
        return base;
    }
    
    public static String[] serializeCoord(WarAgentPercept p){
        String[] res = {Double.toString(p.getDistance()),Double.toString(p.getAngle())};
        return res;
    }
    
    public static Vector2 deserializeCoord(String[] s){
        Vector2 res = new Vector2(Float.parseFloat(s[0]),Float.parseFloat(s[1]));
        return res;
    }
    
    public static Vector2 getCoordPolaireSend(WarBrain bc, WarMessage m){
        Vector2 cibleFromSender = deserializeCoord(m.getContent());
        return VUtils.addPolars(m.getDistance(),m.getAngle(),cibleFromSender.x,cibleFromSender.y);
    }
}
