package org.cwilt.search.domains.asteroids.planner.kasra;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.LinkedList;

/**
 *
 * @author root
 */
public class Hashtable {
    @SuppressWarnings("unchecked")
	public Hashtable()
    {
        TableSize = 100;
        //HashTable = new LinkedList<LinkedList<SearchTreeNode>>();
        Hashtable = (LinkedList<SearchTreeNode>[]) new LinkedList[TableSize];
        
        for (int i = 0; i < TableSize; i++)
        {
            Hashtable[i] = new LinkedList<SearchTreeNode>();
        }
    }
    
    public Boolean IsInList( SearchTreeNode node)
    {
        int index;
        //index = System.identityHashCode(node.GetStateNode()) % TableSize;
        index = Math.abs(node.GetState().hashCode()) % TableSize ;
       
        LinkedList<SearchTreeNode> HashtableBlock = Hashtable[index];
        
        for (int i = 0; i < HashtableBlock.size(); i++) 
        {
            if(node.GetState().equals( HashtableBlock.get(i).GetState()))
            {
                if(node.GetG() < HashtableBlock.get(i).GetG())
                {
                    HashtableBlock.set(i, node);
                }
                return true;
            } 
        }
        
        return false;
    }
    
    public void Add(SearchTreeNode node)
    {
        //int index = System.identityHashCode(node.GetStateNode()) % TableSize;
        int index = Math.abs(node.GetState().hashCode())  % TableSize;
        
        Hashtable[index].add(node);
    }
    
    /////////////////// Variables ///////////////////
    private int TableSize;
    //private LinkedList<LinkedList<SearchTreeNode>> HashTable;//2D List
    private LinkedList<SearchTreeNode>[] Hashtable;
}
