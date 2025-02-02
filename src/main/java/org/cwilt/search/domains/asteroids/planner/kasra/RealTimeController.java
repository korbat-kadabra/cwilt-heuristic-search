/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cwilt.search.domains.asteroids.planner.kasra;
import java.util.ArrayList;
import java.util.LinkedList;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
import org.cwilt.search.domains.asteroids.planner.AsteroidPlanner;
import org.cwilt.search.domains.asteroids.planner.AsteroidState;
import org.cwilt.search.search.SearchState;

/**
 *
 * @author root
 */
public class RealTimeController extends AsteroidPlanner
{
    //private final Random r;
    public RealTimeController(AsteroidProblem a, int timeout, String[] args)
    {
        super(a, timeout);
        seenlist = new Hashtable();
    }

    @Override
    public AsteroidState nextState()
    {
        ArrayList<SearchState.Child> children = super.getCurrentState().expand();
        if(children.isEmpty()) 
        {
            return null;
        }
        
        LinkedList<AsteroidState> possible_states = new LinkedList<AsteroidState>();
        
        for (int i = 0; i < children.size(); i++)
        {
            possible_states.add((AsteroidState) children.get(i).child);
        }
        
        LinkedList<Double> possible_states_hueristics = new LinkedList<Double>();
        
        for (int i = 0; i < possible_states.size(); i++)
        {
            //System.out.println(possible_states.get(i).getShip().x + ", " + possible_states.get(i).getShip().y);
            SearchTreeNode root = new SearchTreeNode();
            root.SetParent(null);
            root.SetG(0);
            root.SetState(possible_states.get(i));
            root.SetIsGoal(root.GetState().isGoal());
            
            if(!seenlist.IsInList(root))
            {
                SearchTree current_child = new SearchTree(root);

                possible_states_hueristics.add(current_child.Start());
            }
        }
        ///////////////////////
        int min_index = 0;
        double min_hueristic = Double.MAX_VALUE;

        for (int i = 0; i < possible_states_hueristics.size(); i++)
        {
            // System.out.println("ship position: x1=" + possible_states.get(i).getShip().x + ", y=" + possible_states.get(i).getShip().y);
            if(possible_states_hueristics.get(i) < min_hueristic)
            { 
                min_hueristic = possible_states_hueristics.get(i);
                min_index = i;                
            }
        }

        //System.out.println(possible_states.get(min_index).getShip().x + " , " + possible_states.get(min_index).getShip().y);
        return possible_states.get(min_index);
        //return possible_states.get(10);
    }
    
    private Hashtable seenlist;
    
}
