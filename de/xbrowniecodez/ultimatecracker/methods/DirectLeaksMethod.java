package de.xbrowniecodez.ultimatecracker.methods;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class DirectLeaksMethod {
	public static void checkFile(File jarFile) throws Throwable {
		if (!jarFile.exists()) {
			throw new IllegalStateException("Output file not found.");
		}
	}

	private static void writeToFile(ZipOutputStream outputStream, InputStream inputStream) throws Throwable {
		byte[] buffer = new byte[4096];
		try {
			while (inputStream.available() > 0) {
				int data = inputStream.read(buffer);
				outputStream.write(buffer, 0, data);
			}
		} finally {
			inputStream.close();
			outputStream.closeEntry();
		}
	}

	public static void process(File jarFile, File outputFile) throws Throwable {
		ZipFile zipFile = new ZipFile(jarFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
		try {
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					try (InputStream in = zipFile.getInputStream(entry)) {
						ClassReader cr = new ClassReader(in);
						ClassNode classNode = new ClassNode();
						cr.accept(classNode, 0);

						invokeDynamicTransfomer(classNode);
						stringEncryptionTransformer(classNode);
						removeObsoleteInjection(classNode);
						removeBootstrapMethod(classNode);
						removeXORMethod(classNode);
						removeHostsCheckMethod(classNode);
						attrRemover(classNode);
						signatureRemover(classNode);

						ClassWriter cw = new ClassWriter(0);
						classNode.accept(cw);
						ZipEntry newEntry = new ZipEntry(entry.getName());
						newEntry.setTime(System.currentTimeMillis());
						out.putNextEntry(newEntry);
						writeToFile(out, new ByteArrayInputStream(cw.toByteArray()));
					}
				} else {
					entry.setTime(System.currentTimeMillis());
					out.putNextEntry(entry);
					writeToFile(out, zipFile.getInputStream(entry));
				}
			}
		} finally {
			zipFile.close();
			if (out != null) {
				out.close();
			}
		}
	}

	private static void removeObsoleteInjection(ClassNode classNode) {
		Iterator<MethodNode> iterator = classNode.methods.iterator();
		while (iterator.hasNext()) {
			MethodNode methodNode = iterator.next();
			if (methodNode.name.equalsIgnoreCase("onEnable") || methodNode.name.equalsIgnoreCase("onLoad")) {
				InsnList insnNodes = methodNode.instructions;
				AbstractInsnNode insnNode = insnNodes.get(0);
				if (insnNode.getOpcode() == 184 && ((MethodInsnNode) insnNode).name.equalsIgnoreCase("\u0970")
						&& ((MethodInsnNode) insnNode).desc.equalsIgnoreCase("()V")) {
					insnNodes.remove(insnNode);
				}
			}
			if (methodNode.name.equalsIgnoreCase("onEnable") || methodNode.name.equalsIgnoreCase("onLoad")) {
				InsnList insnNodes = methodNode.instructions;
				AbstractInsnNode insnNode = insnNodes.get(0);
				if (insnNode.getOpcode() == 184 && ((MethodInsnNode) insnNode).name.equalsIgnoreCase("\u0971")
						&& ((MethodInsnNode) insnNode).desc.equalsIgnoreCase("()V")) {
					insnNodes.remove(insnNode);
				}
			}
			if (methodNode.name.equalsIgnoreCase("\u0970") && methodNode.desc.equalsIgnoreCase("()V")
					&& methodNode.access == 4170) {
				iterator.remove();
			}
			if (methodNode.name.equalsIgnoreCase("\u0971") && methodNode.desc.equalsIgnoreCase("()V")
					&& methodNode.access == 4170) {
				iterator.remove();
			}
		}
	}

	private static void removeBootstrapMethod(ClassNode classNode) {
		Iterator<MethodNode> iterator = classNode.methods.iterator();
		while (iterator.hasNext()) {
			MethodNode methodNode = iterator.next();
			if (methodNode.desc.equalsIgnoreCase(
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;")) {
				iterator.remove();
			}
		}

		if (classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin")
				|| classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin")) {
			for (Iterator<FieldNode> it = classNode.fields.iterator(); it.hasNext();) {
				FieldNode fieldNode = it.next();
				if (fieldNode.access == 9 && fieldNode.desc.equalsIgnoreCase("I") && fieldNode.name.length() == 36) {
					it.remove();
				} else if (fieldNode.access == 9 && fieldNode.desc.equalsIgnoreCase("Ljava/lang/String;")
						&& fieldNode.name.length() == 36) {
					it.remove();
				}
			}
		}
	}

	private static void removeXORMethod(ClassNode classNode) {
		Iterator<MethodNode> iterator = classNode.methods.iterator();
		while (iterator.hasNext()) {
			MethodNode methodNode = iterator.next();
			if (classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin")
					|| classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin")) {
				if (methodNode.name.equalsIgnoreCase("\u0972") && methodNode.access == 4170
						&& methodNode.desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/String;")) {
					iterator.remove();
				}
			}
		}
	}

	private static void removeHostsCheckMethod(ClassNode classNode) throws Throwable {
		Iterator<MethodNode> iterator = classNode.methods.iterator();
		while (iterator.hasNext()) {
			MethodNode attributeIterator = (MethodNode) iterator.next();
			if (attributeIterator.name.equalsIgnoreCase("onEnable")) {
				InsnList attribute = attributeIterator.instructions;
				AbstractInsnNode insnNode = attribute.get(0);
				AbstractInsnNode insnNode1 = attribute.get(1);
				AbstractInsnNode insnNode2 = attribute.get(2);
				if (insnNode.getType() == 5 && insnNode.getOpcode() == 184
						&& ((MethodInsnNode) insnNode).desc.equals("()V")) {
					attribute.remove(insnNode);
					attribute.remove(insnNode1);
					attribute.remove(insnNode2);
				}
			} else if (attributeIterator.desc.equals("()V") && attributeIterator.access == 9) {
				iterator.remove();
			} else if (attributeIterator.desc.equals("(Ljava/lang/String;)Ljava/lang/String;")
					&& attributeIterator.access == 4170) {
				iterator.remove();
			}
		}

	}

	private static void invokeDynamicTransfomer(ClassNode classNode) {
		String bootstrapDesc = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;";
		for (MethodNode methodNode : classNode.methods) {
			InsnList copy = copyInsnList(methodNode.instructions);
			for (int i = 0; i < copy.size(); i++) {
				AbstractInsnNode insn = copy.get(i);
				if (insn instanceof InvokeDynamicInsnNode) {
					InvokeDynamicInsnNode dyn = (InvokeDynamicInsnNode) insn;
					if (dyn.bsmArgs.length == 3) {
						Handle bootstrap = dyn.bsm;
						if (bootstrap.getDesc().equals(bootstrapDesc)) {
							int legitOpCode = (Integer) dyn.bsmArgs[2];
							String legitOwner = dyn.bsmArgs[0].toString().substring(1,
									dyn.bsmArgs[0].toString().length() - 1);
							String legitDesc = dyn.bsmArgs[1].toString();
							MethodInsnNode replacement;
							if (legitOpCode == 182) { // INVOKEVIRTUAL
								replacement = new MethodInsnNode(182, legitOwner, decryptionArray(dyn.name),
										decryptionArray(legitDesc), false);
								methodNode.instructions.set(insn, replacement);
							} else if (legitOpCode == 184) { // INVOKESTATIC
								replacement = new MethodInsnNode(184, legitOwner, decryptionArray(dyn.name),
										decryptionArray(legitDesc), false);
								methodNode.instructions.set(insn, replacement);
							}
						}
					}
				}
			}
		}
	}

	public static InsnList copyInsnList(InsnList original) {
		InsnList newInsnList = new InsnList();

		for (AbstractInsnNode insn = original.getFirst(); insn != null; insn = insn.getNext()) {
			newInsnList.add(insn);
		}

		return newInsnList;
	}

	private static void stringEncryptionTransformer(ClassNode classNode) {
		if (classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin")
				|| classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin")) {
			for (MethodNode methodNode : classNode.methods) {
				InsnList nodes = methodNode.instructions;
				for (int i = 0; i < nodes.size(); i++) {
					AbstractInsnNode instruction = nodes.get(i);
					if (instruction instanceof LdcInsnNode) {
						if (instruction.getNext() instanceof MethodInsnNode) {
							LdcInsnNode ldc = (LdcInsnNode) instruction;
							MethodInsnNode methodinsnnode = (MethodInsnNode) ldc.getNext();
							if (ldc.cst instanceof String) {
								if (methodinsnnode.name.equalsIgnoreCase("\u0972") && methodinsnnode.desc
										.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/String;")) {
									methodNode.instructions.remove(methodinsnnode);
									ldc.cst = decryptionArray((String) ldc.cst);
								}
							}
						}
					}
				}
			}
		}
	}

	private static String decryptionArray(String msg) {
		try {
			char[] array = { '\u4831', '\u2384', '\u2385', '\u9812', '\u9123', '\u4581', '\u0912', '\u3421', '\u0852',
					'\u0723' };
			char[] charArray = msg.toCharArray();
			char[] array2 = new char[charArray.length];
			for (int i = 0; i < charArray.length; ++i) {
				array2[i] = (char) (charArray[i] ^ array[i % array.length]);
			}
			return new String(array2);
		} catch (Exception ex) {
			return msg;
		}
	}

	private static void signatureRemover(ClassNode classNode) {
		if (classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin")
				|| classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin")) {
			classNode.signature = null;
		}
	}

	private static void attrRemover(ClassNode classNode) {
		if (classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin")
				|| classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin")) {
			if (classNode.attrs != null) {
				Iterator<Attribute> attributeIterator = classNode.attrs.iterator();
				while (attributeIterator.hasNext()) {
					Attribute attribute = attributeIterator.next();
					if (attribute.type.equalsIgnoreCase("PluginVersion")) {
						attributeIterator.remove();
					}
					if (attribute.type.equalsIgnoreCase("CompileVersion")) {
						attributeIterator.remove();
					}
				}
			}
		}
	}
}
