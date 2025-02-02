package org.cwilt.search.domains.robot;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.algs.utils.ReverseEnum;
import org.cwilt.search.domains.robot.RobotState.DXY;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.SearchState.Child;
import org.junit.Test;
public class RobotTest {
	@Test
	public void dxyTest() {
		for (int heading : RobotState.headings) {
			DXY f = RobotState.getDXY(heading);
			DXY r = RobotState.getrevDXY(heading);
			if (f.x * -1 != r.x || f.y * -1 != r.y) {
				assert (false);
			}
		}
	}

	@Test
	public void expandTest() throws IOException {
		RobotState initial = new RobotState(null, 80 / 40, 160 / 40, 23, 140);
		RobotProblem p = new RobotProblem(
				"/home/aifs2/cmo66/cjava/santa/robotdata/misc/micro", initial);
		initial = p.getInitial();
		System.err.println(initial);

		RobotState other = new RobotState(null, 40 / 40, 160 / 40, 45, 180);
		RobotProblem p2 = new RobotProblem(
				"/home/aifs2/cmo66/cjava/santa/robotdata/misc/micro", other);
		other = p2.getInitial();
		System.err.println(other);

		ArrayList<Child> children = initial.reverseExpand();
		boolean found = false;
		for (Child c : children) {
			SearchState s = c.child;
			if (s.equals(other)) {
				found = true;
			}
		}
		assert (found);

		found = false;
		children = other.expand();
		for (Child c : children) {
			SearchState s = c.child;
			if (s.equals(initial)) {
				found = true;
			}
		}
		assert (found);
	}

	@Test
	public void longExpandTest() {

		int[] angles = { 0, 45, 90, 135, 180, 225, 270, 315 };

		for (int baseangle : angles) {
			DXY d = RobotState.getDXY(baseangle);
			for (int angle : RobotState.headings) {
				for (int speed : RobotState.speeds) {
					RobotState start = new RobotState(null, 2 + d.x, 2 + d.y,
							angle, speed);
					RobotProblem p2 = new RobotProblem(start);
					// get all of the forward expansion children
					SearchState root = p2.getInitial();
					for (Child c : root.expand()) {
						SearchState s = c.child;
						boolean foundParent = false;
						for (Child recv : s.reverseExpand()) {
							if (recv.child.getKey().equals(start.getKey())) {
								foundParent = true;
								double toChild = recv.transitionCost;
								double toParent = c.transitionCost;
								assert (toParent == toChild);
							}
						}
						assert (foundParent);
					}

					// get all of the reverse expanded children
					for (Child c : root.reverseExpand()) {
						SearchState s = c.child;
						boolean foundParent = false;
						for (Child recv : s.expand()) {
							if (recv.child.getKey().equals(start.getKey())) {
								double toChild = recv.transitionCost;
								double toParent = c.transitionCost;
								assert (toParent == toChild);
								foundParent = true;
							}
						}
						assert (foundParent);
					}
				}
			}
		}
	}

	public void test1() throws IOException, ParseException {
		// RobotProblem p = new
		// RobotProblem("/home/aifs2/group/data/dyn_robot_instances/instance/liney/200/200/25/9");
		RobotProblem p = new RobotProblem(
				"/home/aifs2/cmo66/cjava/santa/robotdata/misc/micro");

		ReverseEnum re = new ReverseEnum(p, new Limit());
		re.solve();

		System.err.println(re.lookup(new RobotState(null, 40 / 40, 160 / 40,
				45, 180)));
		System.err.println(re.lookup(new RobotState(null, 80 / 40, 160 / 40,
				23, 140)));
		System.err.println(re.lookup(new RobotState(null, 120 / 40, 160 / 40,
				0, 100)));
		System.err.println(re.lookup(new RobotState(null, 160 / 40, 160 / 40,
				0, 20)));
		System.err.println(re.lookup(new RobotState(null, 160 / 40, 160 / 40,
				0, 0)));

	}

	public void test2() throws IOException {
		RobotState initial = new RobotState(null, 40 / 40, 160 / 40, 45, 180);
		RobotProblem p = new RobotProblem(
				"/home/aifs2/cmo66/cjava/santa/robotdata/misc/micro", initial);
		AStar a = new AStar(p, new Limit());
		System.err.println(SearchAlgorithm.printPath(a.solve()));
		a.printSearchData(System.err);
	}
}
