package dota2drafter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class Team {
    String name;
    private String[] heroPool = new String[Global.NUMBER_OF_HEROES];
    int poolNumber;
    private int[] players = {-1, -1, -1, -1, -1, -1};
    private int numOfPlayers;
    int globalIndex;
    long uniqueID;
    
    public Team(String n) {
        name = n;
        poolNumber = 0;
        numOfPlayers = 0;
        globalIndex = -1;
        int cat = 41;
    }
    
    Hero[] getHeroPool() {
        Hero[] returnList = new Hero[numOfPlayers];
        for (int i=0; i < poolNumber; i++) {
            returnList[i] = Global.AllHeroes.get(heroPool[i]);
        }
        return returnList;
    }
    
    Player[] getPlayers(){
        Player[] returnList = new Player[numOfPlayers];
        for (int i=0; i < numOfPlayers; i++) {
            returnList[i] = Global.Players.get(players[i]);
        }
        return returnList;
    }
    
    int addPlayer(Player player) {
        if (numOfPlayers < 5) {
            players[numOfPlayers] = player.globalIndex;
            numOfPlayers++;
            if (globalIndex != -1) {
                player.teams.add(globalIndex);
            }
            return 0;
        } else {
            return 1;
        }
    }
    
    int removePlayer(Player player) {
        int returnVal = 0;
        for (int i=0; i < numOfPlayers; i++) {
            if (player.globalIndex == players[i]) {
                returnVal = 1;
                numOfPlayers--;
                if (globalIndex != -1) {
                    player.teams.remove(globalIndex);
                }
            }
            if (returnVal == 1) {
                players[i] = players[i+1];
            }
        }
        return returnVal;
    }
    
    // Doesn't actually delete, just adjusts for the deletion.
    void deletePlayer(Player player) {
        boolean found = false;
        for (int i=0; i < numOfPlayers; i++) {
            if (player.globalIndex == players[i]) {
                found = true;
                numOfPlayers--;
            }
            if (found) {
                players[i] = players[i+1];
            }
            if (players[i] > player.globalIndex) {
                players[i]--;
            }
        }
    }
    
    void addToPool(Hero hero) {
        // Don't want duplicates
        if (!Global.existsInPool(hero.abbrv, getHeroPool()) && poolNumber < Global.NUMBER_OF_HEROES) {
           addHero(hero);
        }
    }
    
    void addHero(Hero hero) {
        // Insertion sort
        // MIGHT BE WRONG!!! PLEASE CHECK THIS!!
        int i = poolNumber;
        while(hero.abbrv.compareTo(heroPool[i]) < 0 && i > 0) {
            heroPool[i-1] = heroPool[i];
            i--;
        }
        heroPool[i] = hero.abbrv;
    }
    
    void saveTeam(boolean isNewTeam) {
        if (isNewTeam) {
            globalIndex = Global.Teams.size();
            Global.Teams.add(this);            
            uniqueID = Global.requestUniqueID();
        } else {
            Global.Teams.set(this.globalIndex, this);
        }
        for (int i=0; i < numOfPlayers; i++) {
            Global.Players.get(players[i]).teams.add(globalIndex);          
        }
        writeTeam();
    }
    
    JPanel teamPreview() {
        JPanel panel = new JPanel(new MigLayout("", "grow, fill"));
        JPanel S = new JPanel(new MigLayout("", "grow, fill"));
        for(Player player: getPlayers()) {
            S.add(new JLabel(player.name));
        }
        panel.add(S, "south, growx");
        panel.add(new JLabel(" " + this.name), "west");
        return panel;
    }
    
    void delete(){
        Global.Teams.remove(this);
        for(int i=globalIndex; i < Global.Teams.size(); i++) {
            Global.Teams.get(i).globalIndex--;
        } 
        for (Player player: getPlayers()) {
            Global.Players.get(player.globalIndex).deleteTeam(this);
        }
        // Delete them on hard disk
        File file = new File(Global.EMERGENCY_TEAM_PATH + uniqueID + ".txt");
        file.delete();
    }
    
    void writeTeam() {
        Writer output = null;
        try {
            File file;            
            new File(Global.EMERGENCY_TEAM_PATH).mkdirs();
            file = new File(Global.EMERGENCY_TEAM_PATH + uniqueID + ".txt");
            file.createNewFile();
            output = new BufferedWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
            output.write("Name: " + name + "\n");
            output.write("Hero Pool: ");
            for (int i = 0; i < poolNumber; i++) {
                output.write(heroPool[i] + ",");
            }   
            output.write("\n");
            output.write("GlobalIndex: " + globalIndex + "\n");
            output.write("Players: ");
            for (int i = 0; i < numOfPlayers; i++) {
                output.write("" + players[i]);
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
                //for (Hero hero: getPlayList()) {
                //    output.write(hero.abbrv + ",");
                //}   output.write("\n");
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
