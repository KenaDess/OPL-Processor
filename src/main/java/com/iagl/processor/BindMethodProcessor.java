package com.iagl.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class BindMethodProcessor extends AbstractProcessor<CtMethod>{

	@Override
	public void process(CtMethod method) {	
		
		doGenerateBindMap(method);
	}	
	
	private void doGenerateBindMap(CtMethod<?> method){
		
		String methodName = method.getSimpleName();
		
		if(methodName.equals("configure")){		
			CtBlock body = method.getBody();
			List<CtStatement> statements = body.getStatements();
			
			/*PrintWriter writer;					
			try {
			writer = new PrintWriter("C:/Users/AnaGissel/Desktop/the-file-name.txt");*/
			for(CtStatement statement:statements){				
				
				if(statement.toString().contains("bind(") && statement.toString().contains(".to("))
				{			
					Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
					TypeFilter<CtVariableAccess> statementFilter = new TypeFilter<CtVariableAccess>(filterClass2);
					List<CtVariableAccess> variables = statement.getElements(statementFilter);
					if(variables.size()== 2)	
						if(!SaveMap.containsClass(variables.get(0).getType().toString())){
							SaveMap.saveBinds(variables.get(0).getType().toString(), variables.get(1).getType().toString());
							//writer.println("bind: "+variables.get(0).getType().toString()+" to "+variables.get(1).getType().toString());
						}
				}						
			}				
			/*	writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
}
