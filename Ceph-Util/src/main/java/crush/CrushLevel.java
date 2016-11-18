/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crush;

/**
 *
 * @author Avinash
 */
public class CrushLevel {
    private int levelno;
      private String levelname;
      private int levelReplica; 

    /**
     * @return the levelno
     */
    public int getLevelno() {
        return levelno;
    }

    /**
     * @param levelno the levelno to set
     */
    public void setLevelno(int levelno) {
        this.levelno = levelno;
    }

    /**
     * @return the levelname
     */
    public String getLevelname() {
        return levelname;
    }

    /**
     * @param levelname the levelname to set
     */
    public void setLevelname(String levelname) {
        this.levelname = levelname;
    }

    /**
     * @return the levelReplica
     */
    public int getLevelReplica() {
        return levelReplica;
    }

    /**
     * @param levelReplica the levelReplica to set
     */
    public void setLevelReplica(int levelReplica) {
        this.levelReplica = levelReplica;
    }
}
