//
// Copyright (c) 2023 ZettaScale Technology
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License 2.0 which is available at
// http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
//
// Contributors:
//   ZettaScale Zenoh Team, <zenoh@zettascale.tech>
//

package io.zenoh;

import io.zenoh.exceptions.ZError;
import io.zenoh.keyexpr.KeyExpr;
import io.zenoh.query.Parameters;
import io.zenoh.query.Selector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SelectorTest {

    @Test
    public void selector_fromStringTest() throws ZError {
        var selector = Selector.tryFrom("a/b/c?arg1=val1");
        assertEquals("a/b/c", selector.getKeyExpr().toString());
        assertNotNull(selector.getParameters());
        assertEquals("arg1=val1", selector.getParameters().toString());

        var selector2 = Selector.tryFrom("a/b/c");
        assertEquals("a/b/c", selector2.getKeyExpr().toString());
        assertNull(selector2.getParameters());

        assertThrows(ZError.class, () -> Selector.tryFrom(""));
    }

    @Test
    public void parametersTest() {
        var parameters = Parameters.from("a=1;b=2;c=1|2|3");
        assertEquals(List.of("1", "2", "3"), parameters.values("c"));
    }

    /**
     * Check the queryable properly receives the query's selector with and without parameters.
     */
    @Test
    public void selectorQueryTest() throws ZError, InterruptedException {
        var session = Zenoh.open(Config.loadDefault());
        var queryableKeyExpr = KeyExpr.tryFrom("a/b/**");

        Selector[] receivedQuerySelector = new Selector[1];
        var queryable = session.declareQueryable(queryableKeyExpr, query -> {
                    receivedQuerySelector[0] = query.getSelector();
                    query.close();
                }
        );

        var querySelector = Selector.tryFrom("a/b/c");
        session.get(querySelector, reply -> {
        });
        Thread.sleep(1000);
        assertEquals(querySelector, receivedQuerySelector[0]);

        var querySelector2 = Selector.tryFrom("a/b/c?key=value");
        session.get(querySelector2, reply -> {
        });
        Thread.sleep(1000);
        assertEquals(querySelector2, receivedQuerySelector[0]);

        queryable.close();
        session.close();
    }
}
