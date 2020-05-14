package andreblanke.thaumcraftpatcher;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("SpellCheckingInspection")
public final class WarpEventsClassTransformer implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TARGET_CLASS_NAME = "thaumcraft.common.lib.WarpEvents";

    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
        if (!name.equals(TARGET_CLASS_NAME))
            return basicClass;

        final ClassNode   node   = new ClassNode();
        final ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        LOGGER.info("Transforming {}", TARGET_CLASS_NAME);

        int patchedChatComponentTextInitInsnCount = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode abstractInsn : method.instructions.toArray()) {
                if (abstractInsn.getOpcode() != INVOKESPECIAL)
                    continue;

                final MethodInsnNode insn = (MethodInsnNode) abstractInsn;

                if (isChatComponentTextInitInsn(insn)) {
                    ++patchedChatComponentTextInitInsnCount;
                    transformChatComponentTextInitInsn(method, insn);
                }
            }
        }
        final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);

        LOGGER.info(
            "Patched {} occurrences of net.minecraft.util.ChatComponentText in {}",
            patchedChatComponentTextInitInsnCount,
            TARGET_CLASS_NAME);

        return writer.toByteArray();
    }

    /**
     * Checks whether the supplied {@link MethodInsnNode} represents a constructor invocation of
     * {@link ChatComponentText} taking a single parameter of type {@link String}.
     *
     * @param insn The {@code MethodInsnNode} which is to be checked if it represents a constructor invocation of
     *             {@code ChatComponentText}.
     *
     * @return {@code true} if the provided {@code insn} represents a constructor invocation of
     *         {@code ChatComponentText} expecting a single {@code String} argument, otherwise {@code false}.
     */
    private boolean isChatComponentTextInitInsn(final MethodInsnNode insn) {
        return insn.owner.equals(Type.getInternalName(ChatComponentText.class))
            && insn.name.equals("<init>")
            && insn.desc.equals("(Ljava/lang/String;)V");
    }

    /**
     * Injects
     *
     * <pre>
     * .setChatStyle(new net.minecraft.util.ChatStyle()
     *     .setColor(net.minecraft.util.EnumChatFormatting.DARK_PURPLE)
     *     .setItalic(Boolean.TRUE))
     * </pre>
     *
     * after {@code insn}, representing a constructor invocation of {@link ChatComponentText}, by modifying
     * {@link MethodNode#instructions} of the supplied {@code methodNode}.
     *
     * @param methodNode The {@link MethodNode} containing the provided {@code insn}.
     *
     * @param insn The {@link MethodInsnNode} with opcode {@link org.objectweb.asm.Opcodes#INVOKESPECIAL} representing
     *             a constructor invocation on an instance of {@link ChatComponentText}.
     */
    private void transformChatComponentTextInitInsn(final MethodNode methodNode, final MethodInsnNode insn) {
        final boolean obfuscated = ThaumcraftPatcherFMLLoadingPlugin.isObfuscated();

        final String chatStyleInternalTypeName = Type.getInternalName(ChatStyle.class);

        final InsnList chatStyleInsns = new InsnList();

        // <editor-fold desc="new ChatStyle()">
        /* Push a new ChatStyle instance. */
        chatStyleInsns.add(new TypeInsnNode(NEW, chatStyleInternalTypeName));

        /*
         * Duplicate the reference to the newly created ChatStyle instance, as one will be popped when invoking the
         * constructor during the execution of the next instruction.
         */
        chatStyleInsns.add(new InsnNode(DUP));

        /* Invoke the ChatStyle constructor. */
        chatStyleInsns.add(
            new MethodInsnNode(
                INVOKESPECIAL,
                chatStyleInternalTypeName,
                "<init>",
                "()V",
                false));
        // </editor-fold>
        // <editor-fold desc=".setColor(EnumChatFormatting.DARK_PURPLE)">
        /* Push EnumChatFormatting.DARK_PURPLE. */
        chatStyleInsns.add(
            new FieldInsnNode(
                GETSTATIC,
                Type.getInternalName(EnumChatFormatting.class),
                EnumChatFormatting.DARK_PURPLE.name(),
                "Lnet/minecraft/util/EnumChatFormatting;"));

        /* Invoke ChatStyle.setColor on the newly created ChatStyle instance. */
        chatStyleInsns.add(
            new MethodInsnNode(
                INVOKEVIRTUAL,
                chatStyleInternalTypeName,
                obfuscated ? "func_150238_a" : "setColor",
                "(Lnet/minecraft/util/EnumChatFormatting;)Lnet/minecraft/util/ChatStyle;",
                false));
        // </editor-fold>
        // <editor-fold desc=".setItalic(true)">
        /* Push Boolean.TRUE. */
        chatStyleInsns.add(
            new FieldInsnNode(
                GETSTATIC,
                Type.getInternalName(Boolean.class),
                "TRUE",
                "Ljava/lang/Boolean;"));

        /* Invoke ChatStyle.setItalic on the newly created ChatStyle instance. */
        chatStyleInsns.add(
            new MethodInsnNode(
                INVOKEVIRTUAL,
                chatStyleInternalTypeName,
                obfuscated ? "func_150217_b" : "setItalic",
                "(Ljava/lang/Boolean;)Lnet/minecraft/util/ChatStyle;",
                false));
        // </editor-fold>

        /* Apply the ChatStyle to the ChatComponentText we assume to have been created using 'insn'. */
        chatStyleInsns.add(
            new MethodInsnNode(
                INVOKEVIRTUAL,
                Type.getInternalName(ChatComponentStyle.class),
                obfuscated ? "func_150255_a" : "setChatStyle",
                "(Lnet/minecraft/util/ChatStyle;)Lnet/minecraft/util/IChatComponent;",
                false));

        methodNode.instructions.insert(insn, chatStyleInsns);
    }
}
