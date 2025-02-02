package org.cwilt.search.domains.asteroids.planner.kasra;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author root
 */
public class SpecialQueue {
        public SpecialQueue()
    {
        queue = new LinkedList<SearchTreeNode>();
    }
    
    public void InsertFront(SearchTreeNode node)
    {
        queue.add(0, node);
    }
    
    public void InsertAndSortbyG(SearchTreeNode node)//using insertionsort
    {
        queue.add(node);
        
        int j = queue.size() - 1;

        SearchTreeNode key = queue.get(j);
        int i = j - 1;

        while (i >= 0 && queue.get(i).GetG() > key.GetG()) 
        {
            queue.set(i + 1, queue.get(i));
            i--;
        }
        queue.set(i + 1, key);
    }
    
//    public void InsertAndSortbyH(SearchTreeNode node)//using insertionsort
//    {
//        queue.add(node);
//        
//        int j = queue.size() - 1;
//
//        SearchTreeNode key = queue.get(j);
//        int i = j - 1;
//
//        while (i >= 0 && queue.get(i).GetH() > key.GetH()) 
//        {
//            queue.set(i + 1, queue.get(i));
//            i--;
//        }
//        queue.set(i + 1, key);
//        
//        //if elements in the front of queue have same h, priority is with element which is dirt place
//        
//         i = 1;
//         while(queue.size() > 1 && queue.get(0).GetH() == queue.get(i).GetH())
//         {
//             if(queue.get(i).GetState().isVaccuumOnDirtPlace || "V".equals(queue.get(i).GetAction()))
//             {
//                 Exchange(0, i);
//                 break;
//             }
//             i++;
//             if(i == queue.size())
//             {
//                 break;
//             }
//         }
//        
//    }
    
    public void InsertAndSortbyF(SearchTreeNode node)//using insertionsort
    {
        queue.add(node);
        
        int j = queue.size() - 1;

        SearchTreeNode key = queue.get(j);
        int i = j - 1;

        while (i >= 0 && queue.get(i).GetF() > key.GetF()) 
        {
            queue.set(i + 1, queue.get(i));
            i--;
        }
        queue.set(i + 1, key);
    }
    
    public void Push(SearchTreeNode node)//add to the end of the queue
    {
        queue.add(node);
    }
    
    public SearchTreeNode Pop()
    {
        SearchTreeNode firstElement = queue.remove(0);
        return firstElement;
    }
    
    public SearchTreeNode Top()
    {
        SearchTreeNode firstElement = queue.get(0);
        return firstElement;
    }
    
    public SearchTreeNode Get(int i)
    {
        return queue.get(i);
    }
    
    public SearchTreeNode PopLowestF()//does tie breaking
    {
        SearchTreeNode key;
        int i, j;
        if(queue.size() > 1 && queue.get(0).GetF() == queue.get(1).GetF())
        {
           
            i = 1;
            while(queue.size() > i &&  queue.get(0).GetF() == queue.get(i).GetF())
            {
                i++;
            }
             
            for (int k = 1; k < i; k++) 
            { // insertion sort by G, higher G should be at fron of queue
                key = queue.get(k);
                j = k - 1;
                while( j >=0 && queue.get(j).GetG() > key.GetG())
                {
                    queue.set(j + 1, queue.get(j));
                    j--;
                }
                queue.set(j + 1, key);
            }
        }
        return queue.remove(0);
    }
    
    public boolean IsEmpty()
    {
        if (queue.isEmpty() )
        {
            return true;
        }
        return false;
    }

    
    public int Size()
    {
        return queue.size();
    }
    /// ptivate variables ///
    
    private List<SearchTreeNode> queue;


}
