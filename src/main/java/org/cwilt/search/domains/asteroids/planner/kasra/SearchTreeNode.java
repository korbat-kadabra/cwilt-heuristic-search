package org.cwilt.search.domains.asteroids.planner.kasra;
import org.cwilt.search.domains.asteroids.planner.AsteroidState;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kasra Dalvand
 */
public class SearchTreeNode 
{

    public SearchTreeNode() 
    {
        is_goal = false;
    }
    
    public SearchTreeNode(SearchTreeNode anotherNode)
    {
        this.g = anotherNode.GetG();
        this.f = anotherNode.GetF();
        this.h = anotherNode.GetH();
        this.parent = anotherNode.GetParent();
    }
    
    public int GetG()
    {
        return g;
    }
    public double GetH()
    {
        return h;
    }
    public double GetF()
    {
        return f;
    }
    public SearchTreeNode GetParent()
    {
        return parent;
    }
    public AsteroidState GetState()
    {
        return state;
    }
    public boolean IsGoal()
    {
        return is_goal;
    }
    
    public void SetG(int _g)
    {
        g = _g;
    }
    public void SetH(double _h)
    {
        h = _h;
    }
    
    public void SetF(double _f)
    {
        f = _f;
    }
    public void SetParent(SearchTreeNode p)
    {
        parent = p;
    }
    public void SetIsGoal(boolean isgoal)        
    {
        is_goal = isgoal;
    }
    public void SetState(AsteroidState _state)
    {
        state = _state;
    }
    //////////////////////// Private Variables /////////////////////
    
    private int g;
    private double h;
    private double f;
    private boolean is_goal;
    private SearchTreeNode parent;
    private AsteroidState state;

}
