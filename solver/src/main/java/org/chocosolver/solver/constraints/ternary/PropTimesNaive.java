/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static org.chocosolver.util.tools.MathUtils.safeMultiply;

/**
 * V0 * V1 = V2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
@Explained
public class PropTimesNaive extends Propagator<IntVar> {

    protected static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;

    private final IntVar v0;
    private final IntVar v1;
    private final IntVar v2;

    public PropTimesNaive(IntVar v1, IntVar v2, IntVar result) {
        super(new IntVar[]{v1, v2, result}, PropagatorPriority.TERNARY, false);
        this.v0 = vars[0];
        this.v1 = vars[1];
        this.v2 = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = div(0, v2.getLB(), v2.getUB(), v1.getLB(), v1.getUB());
            hasChanged |= div(1, v2.getLB(), v2.getUB(), v0.getLB(), v0.getUB());
            hasChanged |= mul(v2, v0.getLB(), v0.getUB(), v1.getLB(), v1.getUB());
        }
        if (v2.isInstantiatedTo(0) && (v0.isInstantiatedTo(0) || v1.isInstantiatedTo(0))) {
            setPassive();
        }
    }

    @Override
    public final ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(v0.getValue() * v1.getValue() == v2.getValue());
        }
        return ESat.UNDEFINED;
    }

    private boolean div(int vidx, int a, int b, int c, int d) throws ContradictionException {
        int min, max;
        IntVar var = vars[vidx];

        if (a <= 0 && b >= 0 && c <= 0 && d >= 0) { // case 1
            min = MIN;
            max = MAX;
            return var.updateLowerBound(min, this, explain(1 - vidx, 2))
                    | var.updateUpperBound(max, this, explain(1 - vidx, 2));
        } else if (c == 0 && d == 0) // case 2
            fails(explain(1 - vidx, 2));
        else if (c < 0 && d > 0) { // case 3
            max = Math.max(Math.abs(a), Math.abs(b));
            min = -max;
            return var.updateLowerBound(min, this, explain(1 - vidx, 2))
                    | var.updateUpperBound(max, this, explain(1 - vidx, 2));
        } else if (c == 0 && (a > 0 || b < 0)) // case 4 a
            return div(vidx, a, b, 1, d);
        else if (c != 0 && d == 0 && (a > 0 || b < 0)) // case 4 b
            return div(vidx, a, b, c, -1);
        else { // if (c > 0 || d < 0) { // case 5
            float ac = (float) a / c, ad = (float) a / d,
                    bc = (float) b / c, bd = (float) b / d;
            float low = Math.min(Math.min(ac, ad), Math.min(bc, bd));
            float high = Math.max(Math.max(ac, ad), Math.max(bc, bd));
            min = (int) Math.round(Math.ceil(low));
            max = (int) Math.round(Math.floor(high));
            if (min > max) this.fails(explain(1 - vidx, 2));
            return var.updateLowerBound(min, this,
                    explain(1 - vidx, 2)) | var.updateUpperBound(max, this, explain(1 - vidx, 2));
        }
        return false;
    }

    private boolean mul(IntVar var, int a, int b, int c, int d) throws ContradictionException {
        int min = Math.min(Math.min(safeMultiply(a, c), safeMultiply(a, d)), Math.min(safeMultiply(b, c), safeMultiply(b, d)));
        int max = Math.max(Math.max(safeMultiply(a, c), safeMultiply(a, d)), Math.max(safeMultiply(b, c), safeMultiply(b, d)));
        return var.updateLowerBound(min, this, explain(0, 1))
                | var.updateUpperBound(max, this, explain(0, 1));
    }


    private Reason explain(int i, int j) {
        if (lcg()) {
            int[] ps = new int[5];
            ps[1] = vars[i].getMinLit();
            ps[2] = vars[i].getMaxLit();
            ps[3] = vars[j].getMinLit();
            ps[4] = vars[j].getMaxLit();
            return Reason.r(ps);
        }
        return Reason.undef();
    }

}
