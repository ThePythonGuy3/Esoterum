package esoterum.world.blocks.binary;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import esoterum.content.*;
import esoterum.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class NoteBlock extends BinaryBlock{
    public NoteSample[] samples = {
        new NoteSample(EsoSounds.bells, "Bells"),
        new NoteSample(EsoSounds.bass, "Bass"),
        new NoteSample(EsoSounds.saw, "Saw"),
        new NoteSample(EsoSounds.organ, "Organ")
    };

    public String[] noteNames = new String[]{
        "C%o", "C%o#", "D%o",
        "D%o#", "E%o", "F%o",
        "F%o#", "G%o", "G%o#",
        "A%o", "A%o#", "B%o"
    };

    public TextureRegion outputRegion;

    public NoteBlock(String name){
        super(name);
        configurable = saveConfig = true;
        emits = true;
        rotate = true;
        drawRot = false;

        inputs = new boolean[]{false, true, true, true};
        outputs = new boolean[]{true, false, false, false};

        config(IntSeq.class, (NoteBlockBuild b, IntSeq i) -> b.configs = IntSeq.with(i.items));
    }

    @Override
    public void load(){
        super.load();
        outputRegion = Core.atlas.find("esoterum-connection");
        connectionRegion = Core.atlas.find("esoterum-connection");
        region = Core.atlas.find("esoterum-gate-base");
    }

    public class NoteBlockBuild extends BinaryBuild{
        /** Direction, Pitch, Octave, Volume, Note Sample */
        public IntSeq configs = IntSeq.with(2, 0, 2, 10, 0);

        @Override
        public void updateTile(){
            lastSignal = nextSignal;
            nextSignal = signal();
            if(nextSignal && !lastSignal) playSound();
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            Draw.color(lastSignal ? Pal.accent : Color.white);
            Draw.rect(outputRegion, x, y, rotdeg());
            drawConnections();
            Draw.rect(topRegion, x, y);
        }

        public void drawConnections(){
            Draw.color(lastSignal ? Pal.accent : Color.white);
            Draw.rect(connectionRegion, x, y, rotdeg() + 90 * configs.first());
        }

        public void playSound(){
            samples[configs.peek()].octaves[configs.get(2)].play((float)configs.get(3) / 10f, EsoUtil.notePitch(configs.get(1)), 0);
        }

        @Override
        public boolean signal(){
            return getSignal(nb.get(configs.first()), this);
        }

        @Override
        public boolean signalFront(){
            return configs.first() == 2 ? signal() : lastSignal;
        }

        @Override
        public void displayBars(Table table){
            super.displayBars(table);
            table.row();
            table.table(e -> {
                Runnable rebuild = () -> {
                    e.clearChildren();
                    e.row();
                    e.left();
                    e.label(() -> "Note: " + noteString() + " (" + samples[configs.get(4)].name + ")").color(Color.lightGray);
                };

                e.update(rebuild);
            }).left();
        }

        @Override
        public void buildConfiguration(Table table){
            table.setBackground(Styles.black5);
            table.table(n -> {
                n.add("Note: ").right();
                Label noteLabel = n.add(noteString()).left().get();
                noteLabel.update(() -> noteLabel.setText(noteString()));
                n.row();
                n.add("Octave: ").right();
                n.table(b -> {
                    b.button("-", () -> {
                        configs.incr(2, -1);
                        if(configs.get(2) < 0){
                            configs.set(2, 0);
                        }
                        configure(configs);
                        playSound();
                    }).size(48f).growX();
                    b.button("+", () -> {
                        configs.incr(2, 1);
                        if(configs.get(2) > 4){
                            configs.set(2, 4);
                        }
                        configure(configs);
                        playSound();
                    }).size(48f).growX();
                }).left();
                n.row();
                n.add("Pitch: ").right();
                n.table(b -> {
                    b.button("-", () -> {
                        configs.incr(1, -1);
                        if(configs.get(1) < 0){
                            configs.set(1, 0);
                        }
                        configure(configs);
                        playSound();
                    }).size(48f).growX();
                    b.button("+", () -> {
                        configs.incr(1, 1);
                        if(configs.get(1) > 11){
                            configs.set(1, 11);
                        }
                        configure(configs);
                        playSound();
                    }).size(48f).growX();
                }).left();
            }).growX().get().background(Tex.underline);
            table.row();
            table.table(s -> {
                s.label(() -> {
                    if(configs.get(4) - 1 >= 0){
                        return samples[configs.get(4) - 1].name;
                    }
                    return "";
                }).color(Color.lightGray).labelAlign(Align.center).right().size(60f, 40f);
                s.button("<", () -> {
                    configs.incr(4, -1);
                    if(configs.get(4) < 0){
                        configs.set(4, 0);
                    }
                    configure(configs);
                    playSound();
                }).size(40f).right();
                s.label(() -> samples[configs.get(4)].name).center().labelAlign(Align.center).size(80f, 40f);
                s.button(">", () -> {
                    configs.incr(4, 1);
                    if(configs.get(4) >= samples.length){
                        configs.set(4, samples.length - 1);
                    }
                    configure(configs);
                    playSound();
                }).size(40f).left();
                s.label(() -> {
                    if(configs.get(4) + 1 < samples.length){
                        return samples[configs.get(4) + 1].name;
                    }
                    return "";
                }).color(Color.lightGray).labelAlign(Align.center).left().size(60f, 40f);
            }).growX().get().background(Tex.underline);
            table.row();
            table.table(v -> {
                v.add("Volume: ").right();
                v.table(b -> {
                    b.button("-", () -> {
                        configs.incr(3, -1);
                        if(configs.get(3) < 0){
                            configs.set(3, 0);
                        }
                        configure(configs);
                        playSound();
                    }).size(48f);
                    TextField vField = b.field(String.valueOf((float)configs.get(3) / 10), vol -> {
                        vol = EsoUtil.extractNumber(vol);
                        if(!vol.isEmpty()){
                            configs.set(3, Math.max((int)(Float.parseFloat(vol) * 10), 0));
                            configure(configs);
                            playSound();
                        }
                    }).labelAlign(Align.center).fillX().size(80, 40).get();
                    vField.update(() -> {
                        Scene stage = vField.getScene();
                        if(!(stage != null && stage.getKeyboardFocus() == vField))
                            vField.setText(String.valueOf((float)configs.get(3) / 10f));
                    });
                    b.button("+", () -> {
                        configs.incr(3, 1);
                        configure(configs);
                        playSound();
                    }).size(48f);
                }).left();
            }).growX().get().background(Tex.underline);
            table.row();
            table.table(b -> {
                b.button(Icon.rotate, () -> {
                    configs.incr(0, -1);
                    if(configs.first() < 1){
                        configs.set(0, 3);
                    }
                    configure(configs);
                }).size(40f);
                b.button("Play", this::playSound).size(120f, 40f);
            });
        }

        @Override
        public Object config(){
            return configs;
        }

        public String noteString(){
            return String.format(noteNames[configs.get(1)], configs.get(2) + 2 + (configs.get(1) >= 9 ? 1 : 0));
        }

        @Override
        public void write(Writes write){
            super.write(write);

            for(int i = 0; i < 5; i++){
                write.i(configs.get(i));
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            configs = IntSeq.with(read.i(), read.i(), read.i(), read.i(), read.i());
        }
    }

    public static class NoteSample{
        /** Array of sounds. Should contain C2, C3, C4, C5, and C6 */
        Sound[] octaves;
        /** Used in config to display the name of the sample */
        String name;

        public NoteSample(Sound[] octaves, String name){
            this.octaves = octaves;
            this.name = name;
        }
    }
}