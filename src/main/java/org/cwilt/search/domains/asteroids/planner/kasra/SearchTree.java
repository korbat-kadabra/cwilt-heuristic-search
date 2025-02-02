/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cwilt.search.domains.asteroids.planner.kasra;
import java.util.ArrayList;
import java.util.LinkedList;

import org.cwilt.search.domains.asteroids.Asteroid;
import org.cwilt.search.domains.asteroids.Ship;
import org.cwilt.search.domains.asteroids.planner.AsteroidState;
import org.cwilt.search.search.SearchState;
/**
 *
 * @author Kasra Dalvand
 */
public class SearchTree 
{
    public SearchTree(SearchTreeNode _root)
    {
        min_f = Double.MAX_VALUE;
        max_g = 2;
        root = _root;
        
        OpenList = new SpecialQueue();
        OpenList.Push(root);
        
        SeenList = new Hashtable();
        SeenList.Add(root);
    }

    public Double Start() 
    {
        while(OpenList.Size() > 0 && OpenList.Top().GetG() < max_g )
        {
            SearchTreeNode currentNode = OpenList.PopLowestF();
   
            if(currentNode.IsGoal())
            {
                return 0.0;
            }
            else
            {
                Expand(currentNode);
            }
            if(OpenList.Size() ==0)
            {
                //System.out.println("zereshk");
            }

            min_f = Double.MAX_VALUE;
            int min_f_index = 0;
            for (int i = 0; i < OpenList.Size(); i++)
            {
                if(OpenList.Get(i).GetF() <= min_f)
                {
                    if(OpenList.Get(i).GetF() == min_f)
                    {
                        if(OpenList.Get(i).GetG() > OpenList.Get(min_f_index).GetG())
                        {
                            min_f = OpenList.Get(i).GetF();
                            min_f_index = i;
                        }
                    }
                    else 
                    {
                        min_f = OpenList.Get(i).GetF();
                        min_f_index = i;
                    }
                }
            }
        }
 
        return min_f;
    }
    
    private LinkedList<AsteroidState> Expand(SearchTreeNode currentNode) 
    {
        ArrayList<SearchState.Child> children = currentNode.GetState().expand();
        if(children.isEmpty()) 
        {
            return null;
        }
        
        LinkedList<AsteroidState> possible_states = new LinkedList<AsteroidState>();

        for (int i = 0; i < children.size(); i++)
        {
            AsteroidState child = (AsteroidState) children.get(i).child;
            SearchTreeNode tree_child = new SearchTreeNode();
            tree_child.SetState(child);
            
            if(!SeenList.IsInList(tree_child))
            {              
                if(!tree_child.GetState().isTerminal())
                {
                    tree_child.SetG(currentNode.GetG() + 1);
                    tree_child.SetH(Heuristic(tree_child));
                    tree_child.SetF(tree_child.GetG() + tree_child.GetH());

                    tree_child.SetParent(currentNode);
                    tree_child.SetIsGoal(tree_child.GetState().isGoal());

                    OpenList.InsertAndSortbyF(tree_child);
                    SeenList.Add(tree_child);

                    possible_states.add(child);
                }
                else
                {
                   // return null;
                }
            }
        }
        
        return possible_states;
    }
    
    
    private double Heuristic(SearchTreeNode node)
    {
        if(!node.GetState().isGoal())
        {
            AsteroidState state = node.GetState();

            double heuristic_value = Math.pow(100 -((state.getScore()) * 100) / 1500.0, 2);
            heuristic_value += Math.pow(WallHeuristic(state), 1.9);
            heuristic_value += Math.pow(ThreateningAstroids(state), 1.2);
            heuristic_value += Math.pow(Direction_of_astroids_heuristic(state), 1.2);
            heuristic_value += Math.pow( DirectionHeuristic(state), 1.1) ;
            
            return heuristic_value;
        }
        else
        {
            return 0.0;
        }
    }
    
    private double WallHeuristic(AsteroidState state)
    {
        double heuristic_value=0;
        
        Ship ship = state.getShip();
        if(ship.x < 385)
        {
            heuristic_value += (385 - ship.x) ;
            //heuristic_value *= Math.abs(ship.dx ) ;
        }
        else if(ship.x > 385)
        {
            heuristic_value += (ship.x - 385);
            //heuristic_value *= Math.abs(ship.dx) ;
        }
        
        if(ship.y < 300)
        {
            heuristic_value += (300 - ship.y) ;
            //heuristic_value *= Math.abs(ship.dy) ;
        }
        else if( ship.y > 300)
        {
            heuristic_value += ship.y - 300 ;
            //heuristic_value *= Math.abs(ship.dy) ;
        }
        
        //System.out.println(ship.dx + " , " + ship.dy);
        heuristic_value = (heuristic_value * 100) / 670.0;
        
        return heuristic_value;
    }
    
    private double ThreateningAstroids(AsteroidState state)
    {
        double heutistic_value = 0.0 ;
        Ship ship = state.getShip();

//        double min_distance = Double.MAX_VALUE;
//        int min_index = 0;
//        for (int i = 0; i < state.getAsteroids().size(); i++)
//        {
//            double distance = distance(ship, state.getAsteroids().get(i));
//            if(distance < min_distance )
//            {
//                //if(!MoveTowardShip(ship, state.getAsteroids().get(i)))
//                {
//                    min_distance = distance;
//                    min_index = i;  
//                }
//            }
//        }
//        
//         heutistic_value =  100 - ((min_distance * 100) / (400.0));
        int number_of_threatening_astroids = 0;
        for (int i = 0; i < state.getAsteroids().size(); i++)
        {
            //if(Math.abs(ship.x - state.getAsteroids().get(i).x) < 75 && Math.abs(ship.y - state.getAsteroids().get(i).y) < 75)
            if(distance(ship, state.getAsteroids().get(i)) < 1000)
            {
                number_of_threatening_astroids++;
                double distance = distance(ship, state.getAsteroids().get(i));
                heutistic_value += distance;
            }
        }
        if(number_of_threatening_astroids == 0)
        {
            heutistic_value = 0;
        }
        else 
        {
            heutistic_value =  100 - ((heutistic_value * 100) / (number_of_threatening_astroids * 1000));
            //heutistic_value =  ((heutistic_value * 100) / (number_of_threatening_astroids * 70.71));
        }
        return  heutistic_value;
    }
    private double Direction_of_astroids_heuristic(AsteroidState state)
    {
        double heuristic_value = 0.0;
        int number_of_less_45 = 0;
        int number_of_less_90 = 0;
        for (int i = 0; i < state.getAsteroids().size(); i++)
        {
            double angle = Math.toDegrees(Angle_of_Astroid_to_Ship(state.getAsteroids().get(i), state.getShip()));
            if(angle < 45)
            {
                number_of_less_45++;
                heuristic_value += ((45 - angle) * 100) / 45;
                double distance = distance(state.getShip(), state.getAsteroids().get(i));
                heuristic_value *= (500 - distance)/250.0;
            }
            else if(angle < 90)
            {
                number_of_less_90++;
                heuristic_value += ((90 - angle) * 25) / 90;
                double distance = distance(state.getShip(), state.getAsteroids().get(i));
                heuristic_value *= (500 - distance)/250.0;
            }
        }
        
        if(number_of_less_45 != 0)
        {
            if(number_of_less_90 !=0)
            {
                heuristic_value = heuristic_value / (double)number_of_less_45 + (double)number_of_less_90;
            }
            else 
            {
                heuristic_value = heuristic_value / (double)number_of_less_45;
            }
        }
        else if(number_of_less_90 != 0)
        {
            heuristic_value = heuristic_value / (double)number_of_less_90;
        }
        
        
        return heuristic_value;
    }
    private double Angle_of_Astroid_to_Ship(Asteroid astroid, Ship ship)
    {   
        double v1_x = ship.x - astroid.x;
        double v1_y = ship.y - astroid.y;
        
        double v2_x = astroid.vx;
        double v2_y = astroid.vy;
        
        double asteroidDistance = Math.sqrt(v1_x * v1_x + v1_y * v1_y);
        v1_x = v1_x / asteroidDistance;
        v1_y = v1_y / asteroidDistance;

        double dotProduct = v1_x * v2_x + v1_y * v2_y;

        // TODO Auto-generated method stub
        return (Math.acos(dotProduct / Vector_Size(v2_x, v2_y)));
    }
    private double distance(Ship ship, Asteroid astroid)
    {
        double distance = Math.sqrt(Math.pow(astroid.x - ship.x, 2) + Math.pow(astroid.y - ship.y, 2));
        return distance;
    }

    private double DirectionHeuristic(AsteroidState state)
    {
        
        double heuristic_value = 0.0;
        Ship ship = state.getShip();
        
        double min_distance = Double.MAX_VALUE;
        int min_index = 0;
        for (int i = 0; i < state.getAsteroids().size(); i++)
        {
            double distance = distance(ship, state.getAsteroids().get(i));
            if(distance < min_distance )
            {
                if(MoveTowardShip(ship, state.getAsteroids().get(i)))
                {
                    min_distance = distance;
                    min_index = i;  
                }
            }
        }
        
        double teta = Math.toDegrees(ship.angleTo(state.getAsteroids().get(min_index)));
        heuristic_value = (teta * 100) / 180.0;
        return heuristic_value;
    }
//    
    
    private boolean MoveTowardShip(Ship ship, Asteroid asteroid)
    {
        if(asteroid.vx > 0)
        {
            if(ship.x < asteroid.x)
            {
                return false;
            }
        }
        else 
        {
            if(ship.x > asteroid.x)
            {
                return false;
            }
        }
        
        if(asteroid.vy < 0)
        {
            if(ship.y < asteroid.y)
            {
                return false;
            }
        }
        else 
        {
            if(ship.x > asteroid.x)
            {
                return false;
            }
        }
        return true;
    }

    private double Vector_Size(double x, double y)
    {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
    ////////////////// Private Variables ////////////////////
    
    private SearchTreeNode root;
    private Hashtable SeenList;
    private SpecialQueue OpenList;
    
    int max_g;
    double min_f;

}