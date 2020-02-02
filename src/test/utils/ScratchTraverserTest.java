package utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PrepareForTest({IOUtils.class, Files.class, Path.class, Paths.class, ScratchTraverser.class})
@RunWith(PowerMockRunner.class)
public class ScratchTraverserTest {
    private static ScratchTraverser traverser;

    static final Logger logger = LoggerFactory.getLogger(ScratchTraverserTest.class);

    @BeforeClass
    public static void setUpClass() throws IOException, ClassNotFoundException {
        PowerMockito.stub(PowerMockito.method(IOUtils.class, "loadClass")).toReturn(utils.Operator.class);
        traverser = new ScratchTraverser(null, null, null, null);
    }

    @Before
    public void setUp() {
//        this.traverser = null;
    }

    @AfterClass
    public static void tearDownClass() {
        // Do something after ALL tests have been run (run once)
    }

    @After
    public void tearDown() {
        // Do something after each test (run twice in this example)
    }

    @org.junit.Test
    public void traverse() throws Exception {
        // throw new AssertionError("Not Implemented");
        // Mock all disk I/O methods
        Collection<Class> classes  = Arrays.asList(IOUtils.class, Files.class, Path.class, Paths.class);
        for (Class c: classes) {
            PowerMockito.mockStatic(c);
        }
        PowerMockito.when(IOUtils.class,"LoadSEDirectory", any(), any(TreeMap.class)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    TreeMap<Integer, ArrayList<Tree<Block>>> userProjects = (TreeMap<Integer, ArrayList<Tree<Block>>>) args[1];
                    Tree<Block> fakeProject = new Tree<Block>(new Block("fakeObj", "fakeBlock"), "Fake Project 1");
                    ArrayList<Tree<Block>> al = new ArrayList<Tree<Block>>();
                    al.add(fakeProject);
                    userProjects.put(1, al);
                    logger.info("called with arguments: " + Arrays.toString(args));
                    return null;
                });
        this.traverser.traverse();
    }

    /**
     * Mocks all methods called within ScratchTraverser.main() and verifies appropriate calls.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @org.junit.Test
    public void main() throws Exception {
        // throw new AssertionError("Not Implemented");
        // Mock all disk I/O methods
        Collection<Class> classes  = Arrays.asList(IOUtils.class, Files.class, Path.class, Paths.class);
        for (Class c: classes) {
            PowerMockito.mockStatic(c);
        }

        ScratchTraverser mockTraverser = PowerMockito.mock(ScratchTraverser.class);
        Mockito.doNothing().when(mockTraverser).traverse();
        PowerMockito.whenNew(ScratchTraverser.class).withAnyArguments().thenReturn(mockTraverser);
        PowerMockito.stub(PowerMockito.method(IOUtils.class, "loadClass")).toReturn(utils.Operator.class);
        PowerMockito.stub(PowerMockito.method(Files.class, "exists")).toReturn(true);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo));
        String[] args = new String[]{null,null,null,null};
        ScratchTraverser.main(args);
        bo.flush();
        String allWrittenLines = new String(bo.toByteArray());
        assertTrue(allWrittenLines.length() > 0);
        Mockito.verify(mockTraverser,Mockito.times(1)).traverse();
    }
}