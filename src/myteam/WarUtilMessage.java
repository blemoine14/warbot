/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myteam;

import edu.warbot.agents.percepts.WarAgentPercept;

/**
 *
 * @author blemoine02
 */
public class WarUtilMessage {
    public static final String SEARCHING_BASE = "where is base ?";
    public static final String BASE_FOUND = "base here";
    public static final String SEARCHING_FOOD = "where is food ?";
    public static final String FOOD_FOUND = "food here";
    public static final String NEED_HEALTH = "medic !";
    public static final String DISPO = "i'm free";
    public static final String CHOOSE_YOU = "you have the job";
    public static final String NOT_CHOOSE_YOU = "no more job for you";
    public static final String CONFIRM_ENROLMENT = "i want the job !";
    public static final String QUIT_ENROLMENT = "i quit the job";
    public static final String NEED_SOMEONE = "need someone";
    public static final String WHERE_ARE_YOU = "where are you ?";
    public static final String IM_HERE = "i'm here";
    public static final String IM_FINE = "i'm fine";
    public static final String BASE_UNDER_ATTACK = "base is under attack";
    public static final String ROCKET_LANCHER_HERE = "rocket launcher here";
    public static final String ASK_REPORT = "state your status";
    public static final String REPORTING = "this is my status";
    
    public static String[] serializeCoord(WarAgentPercept p){
        String[] res = {Double.toString(p.getDistance()),Double.toString(p.getAngle())};
        return res;
    }
}
