/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myteam;

import edu.warbot.brains.WarBrain;

/**
 *
 * @author blemoine02
 */
public abstract class WTask {
	WarBrain myBrain;
        String message;
        
        public WTask(){}
        
        public WTask(String message){
            this.message = message;
        }
	
	abstract String exec(WarBrain bc);
}
