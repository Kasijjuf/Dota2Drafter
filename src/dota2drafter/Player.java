package dota2drafter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class Player {
    String name;
    private Hero[] playList = new Hero[Global.NUMBER_OF_HEROES + 1];
    // Number of heroes they can play
    private int playNumber;
    // Tracks which teams this player is a part of. (Mostly for deleting purposes)
    List<Integer> teams = new ArrayList<>();
    String DotaBuffLink;
    int globalIndex;
    long uniqueID;
    
    public Player(String n) {
        name = n;
        playNumber = 0;
        globalIndex = -1;
    }
    
    void addHero(Hero hero) {
        // Insertion sort
        int i = playNumber;
        if (playNumber == 0){
            playList[0] = hero;
        } else {
            while(i > 0 && (hero.commonName.compareTo(playList[i-1].commonName)) < 0) {
                playList[i] = playList[i-1];
                i--;
            }
            playList[i] = hero;
        }
        playNumber++;
    }    
    
    void removeHero(Hero hero) {
        boolean found = false;
        for (int i=0; i < playNumber; i++) {
            if (hero.abbrv.matches(playList[i].abbrv)) {
                found = true;
                playNumber--;
            }
            if (found) {
                playList[i] = playList[i+1];
            }
        }
    }
    
    void savePlayer() {
        globalIndex = Global.Players.size();
        Global.Players.add(this);
        uniqueID = Global.requestUniqueID();
        writePlayer();
    }
    
    void delete() {
        Global.Players.remove(this);
        for(int i=globalIndex; i < Global.Players.size(); i++) {
            Global.Players.get(i).globalIndex--;
            Global.Players.get(i).writePlayer();
        }
        for(int team: teams) {
            Global.Teams.get(team).deletePlayer(this);
        }
        // Delete them on hard disk
        File file = new File(Global.EMERGENCY_PLAYER_PATH + uniqueID + ".txt");        
        System.out.println("Deleting: " + Global.EMERGENCY_PLAYER_PATH + uniqueID + ".txt");
        file.delete();
    }
    
    JPanel playerPreview() {
        JPanel panel = new JPanel(new MigLayout("", "grow, fill"));
        JPanel S = new JPanel(new MigLayout("wrap 8"));
        for(int i=0; i < this.playNumber; i++) {
            S.add(new JLabel(playList[i].portraitTiny));
        }
        panel.add(S, "south, growx, width min:pref:300");
        panel.add(new JLabel(" " + this.name), "west");
        return panel;
    }
    
    Hero[] getPlayList() {
        Hero[] returnList = new Hero[playNumber];
        System.arraycopy(playList, 0, returnList, 0, playNumber);
        return returnList;
    }
    
    void deleteTeam(Team teamPassed) {
        for (int i=0; i < teams.size(); i++) {
            if (teamPassed.globalIndex == teams.get(i)) {
                teams.remove(i);
            }
        }
        for (int i=0; i < teams.size(); i++) {        
            if (teams.get(i) > teamPassed.globalIndex) {
                teams.set(i, (teams.get(i) - 1));
            }
        }
        writePlayer();
    }
    
    void writePlayer() {
        Writer output = null;
        try {
            File file;            
            new File(Global.EMERGENCY_PLAYER_PATH).mkdirs();
            file = new File(Global.EMERGENCY_PLAYER_PATH + uniqueID + ".txt");
            file.createNewFile();
            output = new BufferedWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    file))));
            output.write("Name: " + name + "\n");
            output.write("PlayList: ");
            for (Hero hero: getPlayList()) {
                output.write(hero.abbrv + ",");
            }   
            output.write("\n");
            output.write("GlobalIndex: " + globalIndex + "\n");
            output.write("Teams: ");
            for (int team: teams) {
                output.write("" + team);
                output.write(",");
            }
        } catch (FileNotFoundException ex) {
            try {
                File file;
                new File(Global.EMERGENCY_PLAYER_PATH).mkdirs();
                file = new File(Global.EMERGENCY_PLAYER_PATH + uniqueID + ".txt");
                file.createNewFile();
                
                output = new BufferedWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                        file))));
                output.write("Name: " + name + "\n");
                output.write("PlayList: ");
                for (Hero hero: getPlayList()) {
                    output.write(hero.abbrv + ",");
                }   output.write("\n");
                output.write("GlobalIndex: " + globalIndex + "\n");
            } catch (IOException ex1) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
