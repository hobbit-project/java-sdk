package org.hobbit.sdk;

import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;

public class ResultPairImpl implements ResultPair{

    private Result expected;
    private Result actual;
    public ResultPairImpl(Result expected, Result actual){
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public Result getExpected() {
        return expected;
    }

    @Override
    public Result getActual() {
        return actual;
    }
}
