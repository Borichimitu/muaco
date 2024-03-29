package bool;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class MyBooleanExpression {
	private String repr;
	
	private String[] variables;
	
	private Map<Map<String, Boolean>, Boolean> truthTable;
	
	public MyBooleanExpression(String expression) throws ParseException {
		expression = expression.replaceAll(" ", "").replaceAll("!", "~");
		this.repr = expression;
		Pattern pattern = Pattern.compile("[()~&|=>+]");
		String[] vars = pattern.split(expression);
		
		HashSet<String> varsSet = new HashSet<String>(Arrays.asList(vars));
		varsSet.removeAll(Arrays.asList(new String[]{"", "1", "0"}));
		this.variables = varsSet.toArray(new String[0]);
		
		assert variables.length < 26;
		
		String shortExpr = new String(expression);
		for (int i = 0; i < variables.length; i++) {
			shortExpr = shortExpr.replaceAll(variables[i], "" + (char)('a' + i));
		}
		
		BooleanExpression booleanExpression = new BooleanExpression(shortExpr);
		Map<Map<String, Boolean>, Map<BooleanExpression, Boolean>> truthTable = new TruthTable(booleanExpression).getResults();
		this.truthTable = new HashMap<Map<String,Boolean>, Boolean>();
		
		for (Map<String, Boolean> map : truthTable.keySet()) {
			Map<BooleanExpression, Boolean> booleanMap = truthTable.get(map);
			boolean val = booleanMap.containsValue(true);
			this.truthTable.put(map, val);
		}
	}

	public boolean hasSolution() {
		return truthTable.containsValue(true);
	}

	public boolean isTautology() {
		return !truthTable.containsValue(false);
	}

	public boolean equals(MyBooleanExpression other) {
		if (other.repr.equals(repr)) {
			return true;
		}
		
		MyBooleanExpression e = null;
		try {
			e = new MyBooleanExpression("(" + repr + ")=(" + other.repr + ")");
		} catch (ParseException ex) {
			ex.printStackTrace();
            System.exit(1);
		} 
		return e.isTautology();
	}
	
	private Map<MyBooleanExpression, Boolean> hasSolutionWithRes;
	
	public boolean hasSolutionWith(MyBooleanExpression other) {
		if (hasSolutionWithRes == null) {
			hasSolutionWithRes = new HashMap<MyBooleanExpression, Boolean>();
		}
		
		if (hasSolutionWithRes.containsKey(other)) {
			return hasSolutionWithRes.get(other);
		}
		
		MyBooleanExpression e = null;
		try {
			e = new MyBooleanExpression("(" + repr + ")&(" + other.repr + ")");
		} catch (ParseException ex) {
			ex.printStackTrace();
            System.exit(1);
		}
		boolean res = e.hasSolution();
		hasSolutionWithRes.put(other, res);
		return res;
	}	

	public String toString() {
		return repr;
	}
}
