package de.xbrowniecodez.ultimatecracker.methods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.xbrowniecodez.ultimatecracker.Watermarker;

public class SpigotMethod {
	public static String userID;
	private static Pattern USERID_PATTERN = Pattern.compile("user_id=([^&]+)");
	public static void process(File jarFile, File outputFile, int mode) throws Throwable {
		ZipFile zipFile = new ZipFile(jarFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipOutputStream out = (mode == 1) ? new ZipOutputStream(new FileOutputStream(outputFile)) : null;
		try {
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					try (InputStream in = zipFile.getInputStream(entry)) {
						ClassReader cr = new ClassReader(in);
						ClassNode classNode = new ClassNode();
						cr.accept(classNode, 0);
						switch (mode) {
						case 0: {
							if (findID(classNode)) {
								return;
							}
							break;
						}
						case 1: {
							removeMethod(classNode);
							removeID(classNode);
							makeWatermarkV1(classNode);
							ClassWriter cw = new ClassWriter(0);
							classNode.accept(cw);
							ZipEntry newEntry = new ZipEntry(entry.getName());
							newEntry.setTime(System.currentTimeMillis());
							out.putNextEntry(newEntry);
							writeToFile(out, new ByteArrayInputStream(cw.toByteArray()));
							break;
						}
						}
					}
				} else {
					if (mode != 1) {
						continue;
					}
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

	private static byte[] toByteArray(InputStream in) throws Throwable {
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			while (in.available() > 0) {
				out.write(buffer, 0, in.read(buffer));
			}
		} finally {
			in.close();
		}
		return out.toByteArray();
	}

	private static boolean findID(ClassNode classNode) throws Throwable {
		for (MethodNode methodNode : classNode.methods) {
			if (methodNode.name.equalsIgnoreCase("loadConfig0")) {

				Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();
				while (insnIterator.hasNext()) {
					AbstractInsnNode insnNode = (AbstractInsnNode) insnIterator.next();
					String str;
					if ((insnNode.getType() == 9)
							&& ((str = ((LdcInsnNode) insnNode).cst.toString()).contains("spigotmc.org"))) {
						Matcher matcher = USERID_PATTERN.matcher(str);
						if (matcher.find()) {
							userID = matcher.group(1);
							return true;
						}
						throw new IllegalStateException("Could not find Spigot User ID.");
					}
				}
			}
		}
		return false;
	}

	public static void makeWatermarkV1(ClassNode classNode) {
		if (classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin")
				|| classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin")) {
			classNode.methods.add(Watermarker.makeWatermark());
		}
	}

	public static void checkFile(File jarFile) throws Throwable {
		if (userID == null) {
			throw new IllegalStateException();
		}
		if (!jarFile.exists()) {
			throw new IllegalStateException("Output file not found.");
		}
		ZipFile zipFile = new ZipFile(jarFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		try {
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				InputStream in = zipFile.getInputStream(entry);
				byte[] fileContent = toByteArray(in);
				if (new String(fileContent).contains(userID)) {
					throw new Exception("Could not remove IDs in " + entry.getName());
				}
			}
		} finally {
			zipFile.close();
		}
	}

	private static void removeMethod(ClassNode classNode) throws Throwable {
		Iterator<MethodNode> iterator = classNode.methods.iterator();
		while (iterator.hasNext()) {
			MethodNode methodNode = iterator.next();
			if (methodNode.name.equalsIgnoreCase("onEnable")) {
				InsnList insnNodes = methodNode.instructions;
				AbstractInsnNode insnNode = insnNodes.get(0);
				if (insnNode.getType() != 5 || insnNode.getOpcode() != 184
						|| !((MethodInsnNode) insnNode).name.equalsIgnoreCase("loadConfig0")) {
					continue;
				}
				insnNodes.remove(insnNode);
			} else {
				if (!methodNode.name.equalsIgnoreCase("loadConfig0")) {
					continue;
				}
				iterator.remove();
			}
		}
		if (classNode.attrs != null) {
			Iterator<Attribute> attributeIterator = classNode.attrs.iterator();
			while (attributeIterator.hasNext()) {
				Attribute attribute = attributeIterator.next();
				if (attribute.type.equalsIgnoreCase("CompileVersion")) {
					attributeIterator.remove();
				}
			}
		}
	}

	private static void removeID(ClassNode classNode) throws Throwable {
		if (userID == null) {
			throw new IllegalStateException();
		}
		Iterator<FieldNode> fieldIterator = classNode.fields.iterator();
		Iterator<MethodNode> methodIterator = classNode.methods.iterator();
		while (fieldIterator.hasNext()) {
			FieldNode fieldNode = (FieldNode) fieldIterator.next();
			if ((fieldNode.value instanceof String)) {
				String value = (String) fieldNode.value;
				if (!value.isEmpty()) {
					fieldNode.value = value.replace(userID, "%%__USER__%%");
				}
			}
		}
		while (methodIterator.hasNext()) {
			MethodNode methodNode = (MethodNode) methodIterator.next();
			InsnList insnNodes = methodNode.instructions;

			Iterator<AbstractInsnNode> insnIterator = insnNodes.iterator();
			while (insnIterator.hasNext()) {
				AbstractInsnNode insnNode = (AbstractInsnNode) insnIterator.next();
				if (insnNode.getOpcode() == 18) {
					Object constant = ((LdcInsnNode) insnNode).cst;
					if ((constant instanceof String)) {
						((LdcInsnNode) insnNode).cst = ((String) ((LdcInsnNode) insnNode).cst).replace(userID,
								"%%__USER__%%");
					}
				}
			}
		}
	}
}
