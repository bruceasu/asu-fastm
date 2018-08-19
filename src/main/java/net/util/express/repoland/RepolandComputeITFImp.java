package net.util.express.repoland;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.util.express.ComputeITF;

public class RepolandComputeITFImp implements ComputeITF {
	private ArrayList<String> expression = new ArrayList<String>();

	private ArrayList<String> right = new ArrayList<String>();

	public Double compute(String express) {
		StringTokenizer st = new StringTokenizer(express, "+-*/()", true);
		while (st.hasMoreElements()) {
			expression.add(st.nextToken());
		}
		return Double.valueOf(getResult());
	}

	private String getResult() {
		toRight();
		FastmStack<String> aStack = new FastmStack<String>();
		String is = null;
		Iterator<String> it = right.iterator();

		while (it.hasNext()) {
			is = it.next();
			if (Calculate.isOperator(is)) {
				int num = Calculate.operatorNum(is);
				if (num == 1) {
					String op1 = (String) aStack.pop();
					aStack.push(Calculate.singleOp(op1));
				} else if (num == 2) {
					String op1 = (String) aStack.pop();
					String op2 = (String) aStack.pop();
					aStack.push(Calculate.twoResult(is, op1, op2));
				}
			} else {
				aStack.push(is);
			}
		}
		return (String) aStack.pop();
	}

	public boolean judge(String express) {
		StringTokenizer st = new StringTokenizer(express, "+-*/()!|=&", true);
		while (st.hasMoreElements()) {
			expression.add(st.nextToken());
		}
		return Boolean.valueOf(getResult()).booleanValue();
	}

	private void toRight() {
		FastmStack<String> aStack = new FastmStack<String>();
		String operator = null;
		int position = 0;
		while (true) {
			if (Calculate.isOperator(expression.get(position))) {
				if (aStack.isEmpty() || expression.get(position).equals("(")) {
					aStack.push(expression.get(position));
				} else if (expression.get(position).equals(")")) {
					if (!aStack.top().equals("(")) {
						operator = (String) aStack.pop();
						right.add(operator);
					}
				} else {
					if (Calculate.priority(expression.get(position)) <= Calculate.priority(aStack
							.top())
							&& !aStack.isEmpty()) {
						operator = (String) aStack.pop();
						if (!operator.equals("(")) {
							right.add(operator);
						}
					}
					aStack.push(expression.get(position));
				}
			} else {
				right.add(expression.get(position));
			}
			position++;
			if (position >= expression.size()) {
				break;
			}
		}
		while (!aStack.isEmpty()) {
			operator = (String) aStack.pop();
			right.add(operator);
		}
	}
}
