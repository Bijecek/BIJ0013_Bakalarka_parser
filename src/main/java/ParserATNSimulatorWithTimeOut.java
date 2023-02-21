import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class ParserATNSimulatorWithTimeOut extends ParserATNSimulator {

    private Instant start = Instant.now();
    private double timeOut;
    public ParserATNSimulatorWithTimeOut(org.antlr.v4.runtime.Parser parser, ATN atn, DFA[] decisionToDFA,
                                         PredictionContextCache sharedContextCache, double timeOut) {
        super(parser, atn, decisionToDFA, sharedContextCache);
        this.timeOut = timeOut;
    }
    @Override
    protected void closure(ATNConfig config,
                           ATNConfigSet configs,
                           Set<ATNConfig> closureBusy,
                           boolean collectPredicates,
                           boolean fullCtx,
                           boolean treatEofAsEpsilon)
    {

        Duration timeElapsed = Duration.between(start, Instant.now());
        if (timeElapsed.toMinutes() >= timeOut )
        {
            System.out.println("parse too long");
            Exception e = new ParseCancellationException("Too long!!!");
            throw new ParseCancellationException(e);

        }
        super.closure(config, configs, closureBusy,collectPredicates,fullCtx,treatEofAsEpsilon);

    }
}