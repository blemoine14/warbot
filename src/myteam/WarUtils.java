/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myteam;

import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import static edu.warbot.agents.enums.WarAgentType.WarBomb;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.launcher.WarGameConfig;
import edu.warbot.tools.geometry.CartesianCoordinates;
import edu.warbot.tools.geometry.PolarCoordinates;

/**
 *
 * @author x
 */
public class WarUtils {
    
    public static double getSpeed(WarAgentType wat){
        switch(wat){
            case Wall :
                return 0;
            case WarBomb :
                return 0;
            case WarDeathRocket :
                return 3;
            case WarBase :
                return 0;
            case WarBullet :
                return 10;
            case WarEngineer :
                return 1;
            case WarFood :
                return 0;
            case WarHeavy :
                return 0.8;
            case WarKamikaze :
                return 1;
            case WarExplorer :
                return 2;
            case WarLight :
                return 1.8;
            case WarRocket :
                return 5;
            case WarRocketLauncher :
                return 1.2;
            case WarShell :
                return 10;
            case WarTurret :
                return 0;
            default :
                return 0;
        }
    }
    
    public static double getExplosionRadius(WarAgentType wat){
        switch(wat){
            case WarBomb :
                return 40;
            case WarDeathRocket :
                return 20;
            case WarBullet :
                return 0.5;
            case WarRocket :
                return 10;
            case WarShell :
                return 1;
            default :
                return 1;
        }
    }
    
    public static double getRadius(WarAgentType wat){
        return Math.floor(WarGameConfig.getHitboxOfWarAgent(wat).getHeight()/2);
    }
    
    public static PolarCoordinates addPolars(double distance1, double angle1, double distance2, double angle2){
        CartesianCoordinates cc1 = new PolarCoordinates(distance1, angle1).toCartesian();
        CartesianCoordinates cc2 = new PolarCoordinates(distance2, angle2).toCartesian();

        CartesianCoordinates v = new CartesianCoordinates((float) cc1.getX() + (float) cc2.getX(), (float) cc1.getY() + (float) cc2.getY());
        return v.toPolar();
    }
    
    public static double angleIncomingPercept(WarAgentPercept p){
        double res = p.getHeading()-(p.getAngle()+180)%360;
        if(res > 180){
            return res-360;
        }
        else{
            return res;
        }
    }
    
    
}
