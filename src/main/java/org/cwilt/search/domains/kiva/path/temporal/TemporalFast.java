/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cwilt.search.domains.kiva.path.temporal;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
/**
 * The TemporalFast class defines straight movement, either horizontally or
 * vertically, across two or three grid cells.
 * 
 * @author Carmen St. Jean
 * 
 */
public class TemporalFast extends TemporalMove {
    private static final double[] TOTAL_WIDTH = {0, 0,
        4 * SQ_POS + SQ_SIZE,
        6 * SQ_POS + 2 * SQ_SIZE};

    private static final int[] NUM_R = {0, 0, 5, 8};

    private static final double[] WIDTH_R = {0, 0,
        TOTAL_WIDTH[2] / NUM_R[2],
        TOTAL_WIDTH[3] / NUM_R[3]};
    
    private final GridCell middle[];
    private Rectangle2D r[];

    public TemporalFast(GridCell start, GridCell end, DIRECTION dir, int time, NavigationProblem p) {
        super(start, end, dir, dir, time,p);

        int dist = 0;

        if (dir == DIRECTION.HORIZONTAL) {
            dist = Math.abs(start.x - end.x);
            assert (start.y == end.y);
            assert (dist == 2 || dist == 3);

            middle = new GridCell[dist - 1];

            if (start.x < end.x) {
                for (int i = 1; i < dist; i++) {
                    middle[i - 1] = start.g.grid[start.x + i][start.y];
                }
            } else if (start.x > end.x) {
                for (int i = 1; i < dist; i++) {
                    middle[i - 1] = start.g.grid[start.x - i][start.y];
                }
            }
        } else if (dir == DIRECTION.VERTICAL) {
            dist = Math.abs(start.y - end.y);
            assert (start.x == end.x);
            assert (dist == 2 || dist == 3);

            middle = new GridCell[dist - 1];

            if (start.y < end.y) {
                for (int i = 1; i < dist; i++) {
                    middle[i - 1] = start.g.grid[start.x][start.y + i];
                }
            } else if (start.y > end.y) {
                for (int i = 1; i < dist; i++) {
                    middle[i - 1] = start.g.grid[start.x][start.y - i];
                }
            }
        } else {
            middle = null;
        }

        if (dist == 2 || dist == 3) {
            r = new Rectangle2D[NUM_R[dist]];

            for (int i = 0; i < NUM_R[dist]; i++) {
                r[i] = new Rectangle2D.Double(SQ_POS + SQ_SIZE + i * WIDTH_R[dist], SQ_POS,
                        WIDTH_R[dist], SQ_SIZE);
            }
        }
    }

	/**
	 * Draws the temporal fast move of a path.
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 * @param timeStep
	 *            The current time step.
	 * @param frameStep
	 *            The current frame step.
	 */
    @Override
    public void draw(Graphics2D d, int timeStep, int frameStep) {
        super.draw(d, timeStep, frameStep);
        AffineTransform oldTransform = d.getTransform();

        d.translate(GridCell.CELL_SIZE / 2, GridCell.CELL_SIZE / 2);

        d.translate(startPosition.x * GridCell.CELL_SIZE, startPosition.y * GridCell.CELL_SIZE);

        if (startDir == DIRECTION.VERTICAL) {
            if (startPosition.y < endPosition.y) {
                d.rotate(Math.toRadians(90));
            } else {
                d.rotate(Math.toRadians(270));
            }
        } else if (startPosition.x > endPosition.x) {
            d.rotate(Math.toRadians(180));
        }

        d.translate(-GridCell.CELL_SIZE / 2, -GridCell.CELL_SIZE / 2);

        if (key.time > timeStep) {
            d.setColor(pathFutureColor);

            for (Rectangle2D rect : r) {
                d.fill(rect);
            }
        } else if (key.time < timeStep) {
            d.setColor(pathVisitedColor);

            for (Rectangle2D rect : r) {
                d.fill(rect);
            }
        } else {
            double frac = (1.0 / (r.length + 1));

            for (int i = 0; i < r.length; i++) {
                if (frameStep < (i + 1) * frac * KivaProblem.FRAME_RATE) {
                    d.setColor(pathFutureColor);
                    d.fill(r[i]);
                } else {
                    d.setColor(pathVisitedColor);
                    d.fill(r[i]);
                }
            }
        }

        d.setTransform(oldTransform);
    }

    public void reserveSpace(KivaProblem p, Drive d) {
        for (GridCell g : middle) {
            p.res.reserveSpace(g, key.time, d);
        }
        p.res.reserveSpace(super.startPosition, key.time, d);
        p.res.reserveSpace(super.endPosition, key.time, d);
    }
    
    @Override
    protected void getChildren(ArrayList<Move> children) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
