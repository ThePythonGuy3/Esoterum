package esoterum.world.blocks.binary;

// too similar to BinaryRouter?
public class BinaryJunction extends BinaryBlock{
    public BinaryJunction(String name){
        super(name);
        emits = true;

        inputs = new boolean[]{true, true, true, true};
        outputs = new boolean[]{true, true, true, true};
    }

    public class BinaryJunctionBuild extends BinaryBuild {
        @Override
        public void updateTile() {
            super.updateTile();
            lastSignal = false;
            for(BinaryBuild b : nb){
                lastSignal |= getSignal(b, this);
            };
        }

        @Override
        public boolean signalFront() {
            return getSignal(nb.get(2), this);
        }
        @Override
        public boolean signalBack() {
            return getSignal(nb.get(0), this);
        }
        @Override
        public boolean signalLeft() {
            return getSignal(nb.get(3), this);
        }
        @Override
        public boolean signalRight() {
            return getSignal(nb.get(1), this);
        }
    }
}