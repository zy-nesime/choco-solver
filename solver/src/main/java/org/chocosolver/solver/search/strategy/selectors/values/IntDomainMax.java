/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

/**
 * Selects the variable upper bound
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 sept. 2010
 */
public final class IntDomainMax implements IntValueSelector {

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectValue(IntVar var) {
        return var.getUB();
    }

}
