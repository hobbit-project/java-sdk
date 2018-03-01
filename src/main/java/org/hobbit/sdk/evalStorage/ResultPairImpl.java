package org.hobbit.sdk.evalStorage;

import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;

/**
 * A simple structure implementing the {@link ResultPair} interface.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 * https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/test/InMemoryEvaluationStore.java
 */
public class ResultPairImpl implements ResultPair {

    private Result actual;
    private Result expected;

    public ResultPairImpl(){

    }

    public ResultPairImpl(Result expected, Result actual){
        this.expected = expected;
        this.actual = actual;
    }

    public void setActual(Result actual) {
        this.actual = actual;
    }

    public void setExpected(Result expected) {
        this.expected = expected;
    }

    @Override
    public Result getActual() {
        return actual;
    }

    @Override
    public Result getExpected() {
        return expected;
    }
}
