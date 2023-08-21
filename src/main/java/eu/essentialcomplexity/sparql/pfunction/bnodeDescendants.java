package eu.essentialcomplexity.sparql.pfunction;

import java.util.Stack;
import java.util.ArrayList;
import java.util.Iterator ;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropFuncArgType ;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval ;
import org.apache.jena.sparql.util.IterLib ;
import org.apache.jena.util.iterator.ExtendedIterator;


public class bnodeDescendants extends PropertyFunctionEval
{
    public bnodeDescendants()
    {
        super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_LIST) ;
    }

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        // Do some checking.
        // These checks are assumed to be passed in .exec()
        if ( argSubject.isList() )
            throw new QueryBuildException(Lib.className(this)+ "Subject must be a single node or variable, not a list") ;
    }

    @Override
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        try 
        {
            // Object must be unbound variable
            if ( !argObject.getArg().isVariable() )
            {
                Log.warn(this, "Object to property function bnode descendants must be an unbound variable.") ;
                return IterLib.noResults(execCxt) ;
            }
            // Subject bound to literal.
            if ( argSubject.getArg().isLiteral() )
            {
                // Only find blanknode descendants of IRIs.
                Log.warn(this, "Subject to property function bnode descendants is not a URI.") ;
                return IterLib.noResults(execCxt) ;
            }
            if ( argSubject.getArg().isURI() || argSubject.getArg().isBlank() )
                // Case 1 : subject is a fixed URI or a variable bound to a URI.
                return subjectIsIRI(argSubject.getArg(), argObject, binding, execCxt) ;
            
            // Case 2 : subject is an unbound variable.
            Log.warn(this, "Subject to property function bnode descendants is not bound nor a constant.") ;
            return IterLib.noResults(execCxt) ;
        } catch (QueryException ex)
        {
            Log.warn(this, "Unexpected problems in bnode descendants: "+ex.getMessage()) ;
            return null ;
        }
    }

    private QueryIterator subjectIsIRI(Node subject, PropFuncArg argObject, Binding binding, ExecutionContext execCxt)
    {
        final Var objectVar = Var.alloc(argObject.getArg());

        // Retrieve current Graph.
        Graph graph = execCxt.getActiveGraph();
  
        Stack<Node> includedBNodes = new Stack<>();
        includedBNodes.push(subject);
        List<BlankNodeId> objList =  new ArrayList<>();

        while (!includedBNodes.isEmpty())
        {
            Node matchingSubject = includedBNodes.pop();
            ExtendedIterator<Triple> iter = graph.find(matchingSubject, Node.ANY, Node.ANY) ;
            while(iter.hasNext())
            {
                Triple t = iter.next();
                Node objectNode = t.getObject() ;
                if (objectNode.isBlank())
                {
                    if (objList.contains(objectNode.getBlankNodeId()))
                    {
                        Log.error(objectNode, "Cycle detected in blank nodes, illegal condition in CBD entity.");
                    }
                    includedBNodes.push(objectNode);
                    objList.add(objectNode.getBlankNodeId());
                }
            }
        }
        Iterator<Binding> it = Iter.map(
                    objList.iterator(),
                    item -> BindingFactory.binding(binding, objectVar, NodeFactory.createBlankNode(item)));
        return QueryIterPlainWrapper.create(it, execCxt);
    }
}
