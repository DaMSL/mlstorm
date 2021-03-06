package topology.weka;

import backtype.storm.generated.StormTopology;
import backtype.storm.topology.IRichSpout;
import backtype.storm.tuple.Fields;
import storm.trident.Stream;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.state.QueryFunction;
import storm.trident.state.StateFactory;
import storm.trident.state.StateUpdater;

/**
 * User: lbhat <laksh85@gmail.com>
 * Date: 12/16/13
 * Time: 7:31 PM
 */
public class WekaLearningBaseTopology {
    protected static StormTopology buildTopology (final IRichSpout spout,
                                                  final int parallelism,
                                                  final StateUpdater stateUpdater,
                                                  final StateFactory stateFactory,
                                                  final QueryFunction queryFunction,
                                                  final String drpcFunction)
    {
        TridentTopology topology = new TridentTopology();
        Stream featuresStream = topology.newStream("features", spout);

        TridentState state =
                featuresStream
                        .broadcast()
                        .parallelismHint(parallelism)
                        .partitionPersist(stateFactory, new Fields("key", "featureVector"), stateUpdater)
                        .parallelismHint(parallelism)
                ;


        Stream stateStream
                = topology.newDRPCStream(drpcFunction)
                          .broadcast()
                          .stateQuery(state, new Fields("args"), queryFunction, new Fields("result"));

        return topology.build();
    }

}
