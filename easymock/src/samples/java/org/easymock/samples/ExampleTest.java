/**
 * Copyright 2001-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.easymock.samples;

import org.easymock.*;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @author OFFIS, Tammo Freese, Henri Tremblay
 */
public class ExampleTest extends EasyMockSupport {

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @TestSubject
    private ClassTested classUnderTest = new ClassTested();

    @Mock
    private Collaborator mock;

    @Test
    public void removeNonExistingDocument() {
        replayAll();
        classUnderTest.removeDocument("Does not exist");
    }

    @Test
    public void addDocument() {
        mock.documentAdded("New Document");
        replayAll();
        classUnderTest.addDocument("New Document", "content");
        verifyAll();
    }

    @Test
    public void addAndChangeDocument() {
        mock.documentAdded("Document");
        mock.documentChanged("Document");
        expectLastCall().times(3);
        replayAll();
        classUnderTest.addDocument("Document", "content");
        classUnderTest.addDocument("Document", "content");
        classUnderTest.addDocument("Document", "content");
        classUnderTest.addDocument("Document", "content");
        verifyAll();
    }

    @Test
    public void voteForRemoval() {
        // expect document addition
        mock.documentAdded("Document");
        // expect to be asked to vote, and vote for it
        expect(mock.voteForRemoval("Document")).andReturn((byte) 42);
        // expect document removal
        mock.documentRemoved("Document");

        replayAll();
        classUnderTest.addDocument("Document", "content");
        assertTrue(classUnderTest.removeDocument("Document"));
        verifyAll();
    }

    @Test
    public void voteAgainstRemoval() {
        // expect document addition
        mock.documentAdded("Document");
        // expect to be asked to vote, and vote against it
        expect(mock.voteForRemoval("Document")).andReturn((byte) -42); // 
        // document removal is *not* expected

        replayAll();
        classUnderTest.addDocument("Document", "content");
        assertFalse(classUnderTest.removeDocument("Document"));
        verifyAll();
    }

    @Test
    public void voteForRemovals() {
        mock.documentAdded("Document 1");
        mock.documentAdded("Document 2");
        expect(mock.voteForRemovals("Document 1", "Document 2")).andReturn((byte) 42);
        mock.documentRemoved("Document 1");
        mock.documentRemoved("Document 2");
        replayAll();
        classUnderTest.addDocument("Document 1", "content 1");
        classUnderTest.addDocument("Document 2", "content 2");
        assertTrue(classUnderTest.removeDocuments(new String[] { "Document 1", "Document 2" }));
        verifyAll();
    }

    @Test
    public void voteAgainstRemovals() {
        mock.documentAdded("Document 1");
        mock.documentAdded("Document 2");
        expect(mock.voteForRemovals("Document 1", "Document 2")).andReturn((byte) -42);
        replayAll();
        classUnderTest.addDocument("Document 1", "content 1");
        classUnderTest.addDocument("Document 2", "content 2");
        assertFalse(classUnderTest.removeDocuments("Document 1", "Document 2"));
        verifyAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void answerVsDelegate() {
        final List<String> l = createMock(List.class);

        // andAnswer style
        expect(l.remove(10)).andAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return getCurrentArguments()[0].toString();
            }
        });

        // andDelegateTo style
        expect(l.remove(10)).andDelegateTo(new ArrayList<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String remove(final int index) {
                return Integer.toString(index);
            }
        });

        replayAll();

        assertEquals("10", l.remove(10));
        assertEquals("10", l.remove(10));

        verifyAll();
    }
}
