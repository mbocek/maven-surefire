package org.apache.maven.surefire.its.jiras;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;

import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.apache.maven.shared.utils.xml.Xpp3DomBuilder;
import org.apache.maven.surefire.its.fixture.OutputValidator;
import org.apache.maven.surefire.its.fixture.SurefireJUnit4IntegrationTestCase;
import org.junit.Test;

/**
 * Test surefire-report on TestNG test
 *
 * @author <a href="mailto:michal.bocek@gmail.com">Michal Bocek</a>
 */
public class Surefire1057DataProviderExceptionIT
    extends SurefireJUnit4IntegrationTestCase
{
    
    private enum ResultType
    {
        SKIPPED( "skipped" ), FAILURE( "failure" );

        private String type;
        
        private ResultType(String type)
        {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }

    @Test
    public void testNgReport688() throws Exception {
        testNgReport( "6.8.8", ResultType.SKIPPED,
                                   "java.lang.RuntimeException: Exception in data provider",
                                   "java.lang.RuntimeException" );
    }
    

    @Test
    public void testNgReport60() throws Exception {
        testNgReport( "6.0", ResultType.SKIPPED,
                                   "java.lang.RuntimeException: Exception in data provider",
                                   "java.lang.RuntimeException" );
    }

    @Test
    public void testNgReport51410() throws Exception {
        testNgReport( "5.14.10", ResultType.SKIPPED,
                                   "java.lang.RuntimeException: Exception in data provider",
                                   "java.lang.RuntimeException" );
    }
    
    @Test
    public void testNgReport5148() throws Exception {
        testNgReport( "5.14.9", ResultType.SKIPPED,
                                   "java.lang.RuntimeException: Exception in data provider",
                                   "java.lang.RuntimeException" );
    }

    @Test
    public void testNgReport5147() throws Exception {
        testNgReport( "5.14.7", ResultType.SKIPPED,
                                   "java.lang.RuntimeException: Exception in data provider",
                                   "java.lang.RuntimeException" );
    }
    

    @Test
    public void testNgReport5146() throws Exception {
        testNgReport( "5.14.6", ResultType.SKIPPED,
                                   null,
                                   null );
    }

    @Test
    public void testNgReport5141() throws Exception {
        testNgReport( "5.14.1", ResultType.SKIPPED,
                                   null,
                                   null );
    }
    
    @Test
    public void testNgReport514() throws Exception {
        testNgReport( "5.14", ResultType.SKIPPED,
                                   null,
                                   null );
    }

    @Test
    public void testNgReport5131() throws Exception {
        testNgReport( "5.13.1", ResultType.FAILURE,
                                   "java.lang.reflect.InvocationTargetException",
                                   "org.testng.TestNGException" );
    }

    @Test
    public void testNgReport513() throws Exception {
        testNgReport( "5.13", ResultType.FAILURE, 
                                   "java.lang.reflect.InvocationTargetException",
                                   "org.testng.TestNGException" );
    }

    @Test
    public void testNgReport5121() throws Exception {
        testNgReport( "5.12.1", ResultType.FAILURE,
                                   "java.lang.reflect.InvocationTargetException",
                                   "org.testng.TestNGException" );
    }

    @Test
    public void testNgReport57() throws Exception {
        testNgReport( "5.7", ResultType.FAILURE,
                                   "java.lang.reflect.InvocationTargetException",
                                   "org.testng.TestNGException" );
    }
    
    private void testNgReport( String version, ResultType resultType, String message, String type )
        throws Exception
    {
        final OutputValidator outputValidator = runTest( version, resultType );
        
        Xpp3Dom[] children = readTests( outputValidator, "testng.DataProviderExceptionReportTest" );
        assertThat( "Report should contains only one test case", children.length, is( equalTo( 1 ) ));
        
        Xpp3Dom test = children[0];
        assertThat( "Not expected classname", test.getAttribute( "classname" ),
                    is( equalTo( "testng.DataProviderExceptionReportTest" ) ) );
        assertThat( "Not expected test name", test.getAttribute( "name" ),
                    is( equalTo( "testDataProvider" ) ) );

        children = test.getChildren( resultType.getType() );
        assertThat( "Test should contains only one " + resultType.getType() + " element", children.length,
                    is( equalTo( 1 ) ) );

        Xpp3Dom result = children[0];
        if ( message == null )
        {
            assertThat( "Subelement message attribute must be null", result.getAttribute( "message" ), is( nullValue() ) );
        }
        else
        {
            assertThat( "Subelement should contains message attribute", result.getAttribute( "message" ),
                        is( equalTo( message ) ) );
        }

        if ( type == null )
        {
            assertThat( "Subelement type attribute must be null", result.getAttribute( "type" ), is( nullValue() ) );
        } else {
            assertThat( "Subelement should contains type attribute", result.getAttribute( "type" ),
                        is( equalTo( type ) ) );
        }
    }

    private OutputValidator runTest( String version, ResultType resultType  )
    {
        int skipped = ResultType.SKIPPED.equals( resultType ) ? 1 : 0;
        int failure = ResultType.FAILURE.equals( resultType ) ? 1 : 0;
        
        final OutputValidator outputValidator = unpack( "/surefire-1057-dataprovider-exception" )
                        .resetInitialGoals( version )
                        .addSurefireReportGoal()
                        .executeCurrentGoals()
                        .assertTestSuiteResults( 1, 0, failure, skipped );
        return outputValidator;
    }
    
    private Xpp3Dom[] readTests( OutputValidator validator, String className )
        throws FileNotFoundException
    {
        Xpp3Dom testResult =
            Xpp3DomBuilder.build( validator.getSurefireReportsXmlFile( "TEST-" + className + ".xml" ).getFileInputStream(),
                                  "UTF-8" );
        Xpp3Dom[] children = testResult.getChildren( "testcase" );
        return children;
    }
    
}
