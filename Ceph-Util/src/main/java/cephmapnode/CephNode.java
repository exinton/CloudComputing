/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cephmapnode;


import java.io.Serializable;
import java.util.ArrayList;
import net.Address;
/**
 *
 * @author Avinash
 */
public class CephNode implements Serializable{
    private static int id_next = 0;
    
    private int levelNo;
    private String id;
    private String type;
    private double weight;
    private double hiddenWeight;
    
    // isHidden is set for nodes added newly, cleared after data reshuffling
    private boolean isHidden;
    // set for disk storage nodes
    private boolean isDisk;
    
    // takes value between 0 and 1. 
    // if hash(x, r, C_id) < hashRange, then this node should be chosen
    // HIDDEN NODES NOT TAKEN INTO ACCOUNT
    private double hashRange;
    
    // |||r to hashRange, but hidden nodes ARE taken into account
    // i.e. this serves as NEW CEPH MAP after modification
    private double hiddenHashRange;
    
    // path info where the files are to be stored
    private String driveInfo;

    // set when node deleted by admin
    private boolean isFailed;
    
    // set when disk node is overloaded, marked by admin
    private boolean isOverloaded;
    
    // structure containing ip address, port
    private Address address;
    
    private boolean isAlreadySelected;
    
    private ArrayList<CephNode> children;
    
    private boolean underProcessing;
    
    
    public CephNode(){
//        this.id = Long.toString(System.currentTimeMillis());
        this.id = Integer.toString(id_next++);
        
        this.weight = 0.0D;
        this.hiddenWeight = 0.0D;
        this.isDisk = false;
        this.hashRange = 0.0D;
        this.hashRange = 0.0D;
        this.isFailed = false;
        this.isHidden = false;
        this.children = new ArrayList<>();
        this.address = null;
        this.underProcessing = false;
    }
    
/**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    /**
     * @return the hiddenWeight
     */
    public double getHiddenWeight() {
        return this.hiddenWeight;
    }

    /**
     * @param hiddenWeight the hiddenWeight to set
     */
    public void setHiddenWeight(double hiddenWeight) {
        this.hiddenWeight = hiddenWeight;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the driveInfo
     */
    public String getDriveInfo() {
        return driveInfo;
    }

    /**
     * @param driveInfo the driveInfo to set
     */
    public void setDriveInfo(String driveInfo) {
        this.driveInfo = driveInfo;
    }

    /**
     * @return the isFailed
     */
    public boolean getIsFailed() {
        return isFailed;
    }

    /**
     * @param isFailed the isFailed to set
     */
    public void setIsFailed(boolean isFailed) {
        this.isFailed = isFailed;
    }

    /**
     * @return the hashRange
     */
    public double getHashRange() {
        return hashRange;
    }

    /**
     * @param hashRange the hashRange to set
     */
    public void setHashRange(double hashRange) {
        this.hashRange = hashRange;
    }
    
    /**
     * @return the hiddenHashRange
     */
    public double getHiddenHashRange() {
        return this.hiddenHashRange;
    }

    /**
     * @param hiddenHashRange the hiddenHashRange to set
     */
    public void setHiddenHashRange(double hiddenHashRange) {
        this.hiddenHashRange = hiddenHashRange;
    }
    
    public void setIsDisk(boolean isDisk){
    	this.isDisk = isDisk;
    }
    
    public boolean getIsDisk(){
    	return this.isDisk;
    }
    
    public void setIsOverloaded(boolean isOverloaded){
        this.isOverloaded = isOverloaded;
    }
    
    public boolean getIsOverloaded(){
        return this.isOverloaded;
    }
    
    public void setAddress(String ip, int port){
    	this.address = new Address(ip, port);
    }
    
    public void setAddress(Address addr){
        this.address = new Address(addr.getIp(), addr.getPort());
    }
    
    public Address getAddress(){
        return this.address;
    }
    
    /**
     * @return the children
     */
    public ArrayList<CephNode> getChildren() {
        return this.children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(ArrayList<CephNode> children) {
        this.children = children;
    }
    
    public void addChild(CephNode node){
        if(this.children != null){
            ArrayList<CephNode> new_list = new ArrayList<>();
            new_list.add(node);
            new_list.addAll(this.children);
            this.children = new_list;
        }
    }
    
    public void setIsHidden(boolean isHidden){
        this.isHidden = isHidden;
    }
    
    public boolean getIsHidden(){
        return this.isHidden;
    }    

    /**
     * @return the levelNo
     */
    public int getLevelNo() {
        return levelNo;
    }

    /**
     * @param levelNo the levelNo to set
     */
    public void setLevelNo(int levelNo) {
        this.levelNo = levelNo;
    }
    
    public void print() {
        System.out.println("id of the node " + this.getId());
        System.out.println(" level  " + this.getLevelNo());
        System.out.println("id of the node " + this.getId());
        System.out.println("id of the node " + this.getId());
    }

    /**
     * @return the isAlreadySelected
     */
    public boolean isIsAlreadySelected() {
        return isAlreadySelected;
    }

    /**
     * @param isAlreadySelected the isAlreadySelected to set
     */
    public void setIsAlreadySelected(boolean isAlreadySelected) {
        this.isAlreadySelected = isAlreadySelected;
    }
    
    public boolean getUnderProcessing() {
        return this.underProcessing;
    }
    
    public void setUnderProcessing(boolean underProcessing) {
        this.underProcessing = underProcessing;
    }
}