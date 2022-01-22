/**
 * Copyright (c) 2016-2020, Mihai Emil Andronache
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.amihaiemil.eoyaml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base implementation of YamlPrinter. "Rt" stands for "Runtime".
 *
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: afa3de41d3c85ac89058e3fcb674e009672d8b2f $
 * @checkstyle ExecutableStatementCount (400 lines)
 * @checkstyle CyclomaticComplexity (400 lines)
 * @since 4.3.1
 */
public final class ExtensionYamlPrinter implements YamlPrinter {

    /**
     * Writer where the given YAML will be printed.
     */
    private final Writer writer;

    /**
     * Constructor.
     *
     * @param writer Destination writer.
     */
    public ExtensionYamlPrinter(final Writer writer) {
        this.writer = writer;
    }

    @Override
    public void print(final YamlNode node) throws IOException {
        try {
            if (node instanceof Scalar) {
                this.writer.append("---").append(System.lineSeparator());
                this.printPossibleComment(node, "");
                this.printScalar((Scalar) node, 0);
                this.writer.append(System.lineSeparator()).append("...");
            } else if (node instanceof YamlSequence) {
                boolean documentComment = this.printPossibleComment(node, "");
                if (documentComment) {
                    this.writer.append("---").append(System.lineSeparator());
                }
                this.printSequence((YamlSequence) node, 0);
            } else if (node instanceof YamlMapping) {
                boolean documentComment = this.printPossibleComment(node, "");
                if (documentComment) {
                    this.writer.append("---").append(System.lineSeparator());
                }
                this.printMapping((YamlMapping) node, 0);
            } else if (node instanceof YamlStream) {
                this.printStream((YamlStream) node, 0);
            }
        } finally {
            this.writer.close();
        }
    }

    /**
     * Print a YAML Stream of documents.
     *
     * @param stream      Given YamlStream.
     * @param indentation Level of indentation of the printed stream.
     * @throws IOException If an I/O problem occurs.
     */
    private void printStream(
            final YamlStream stream,
            final int indentation
    ) throws IOException {
        final String newLine = System.lineSeparator();
        int spaces = indentation;
        final StringBuilder indent = new StringBuilder();
        while (spaces > 0) {
            indent.append(" ");
            spaces--;
        }
        final Iterator<YamlNode> valuesIt = stream.values().iterator();
        while (valuesIt.hasNext()) {
            final YamlNode document = valuesIt.next();
            this.writer
                    .append(indent)
                    .append("---");
            this.printNode(document, true, indentation + 2);
            if (valuesIt.hasNext()) {
                this.writer.append(newLine);
            }
        }
    }

    /**
     * Print a YAML Mapping.
     *
     * @param mapping     Given YamlMapping.
     * @param indentation Level of indentation of the printed mapping.
     * @throws IOException If an I/O problem occurs.
     */
    private void printMapping(
            final YamlMapping mapping,
            final int indentation
    ) throws IOException {
        final String newLine = System.lineSeparator();
        int spaces = indentation;
        final StringBuilder alignment = new StringBuilder();
        while (spaces > 0) {
            alignment.append(" ");
            spaces--;
        }
        final Iterator<YamlNode> keysIt = mapping.keys().iterator();
        while (keysIt.hasNext()) {
            final YamlNode key = keysIt.next();
            final YamlNode value = mapping.value(key);
            this.printPossibleComment(value, alignment.toString());
            this.writer.append(alignment);
            if (key instanceof Scalar) {
                this.writer.append(
                        this.indent(
                                ((Scalar) key).value(),
                                0
                        )
                );
                this.writer
                        .append(":");
            } else {
                this.writer
                        .append("?");
                this.printNode(key, true, indentation + 2);
                this.writer.append(newLine)
                        .append(alignment)
                        .append(":");
            }
            if (value instanceof Scalar) {
                this.printNode(value, false, indentation);
            } else {
                this.printNode(value, true, indentation + 2);
            }
            if (keysIt.hasNext()) {
                this.writer.append(newLine);
            }
        }
    }

    /**
     * Print a YAML Sequence.
     *
     * @param sequence    Given YamlSequence.
     * @param indentation Level of indentation of the printed Scalar.
     * @throws IOException If an I/O problem occurs.
     */
    private void printSequence(
            final YamlSequence sequence,
            final int indentation
    ) throws IOException {
        final String newLine = System.lineSeparator();
        int spaces = indentation;
        final StringBuilder alignment = new StringBuilder();
        while (spaces > 0) {
            alignment.append(" ");
            spaces--;
        }
        final Iterator<YamlNode> valuesIt = sequence.values().iterator();
        while (valuesIt.hasNext()) {
            final YamlNode node = valuesIt.next();
            this.printPossibleComment(node, alignment.toString());
            this.writer
                    .append(alignment)
                    .append("-");
            if (node instanceof Scalar) {
                this.printNode(node, false, 0);
            } else {
                //修复输出格式不正确问题
                if (node instanceof YamlMapping && !node.isEmpty()) {
                    YamlMapping originMapping = (YamlMapping) node;
                    List<YamlNode> originKeyList = new ArrayList<>(originMapping.keys());
                    int keyLength = originKeyList.size();
                    //first key-value
                    YamlNode firstKeyNode = originKeyList.get(0);
                    YamlMapping firstYamlMapping = Yaml.createYamlMappingBuilder()
                            .add(firstKeyNode, originMapping.value(firstKeyNode))
                            .build();
                    this.printNode(firstYamlMapping, false, 0);
                    //more key-value
                    if (keyLength > 1) {
                        YamlMappingBuilder subYamlMappingBuilder = Yaml.createYamlMappingBuilder();
                        for (int i = 1; i < keyLength; i++) {
                            YamlNode keyNode = originKeyList.get(i);
                            YamlNode valueNode = originMapping.value(keyNode);
                            subYamlMappingBuilder = subYamlMappingBuilder.add(keyNode, valueNode);
                        }
                        this.printNode(subYamlMappingBuilder.build(), true, indentation + 2);
                    }
                } else {
                    this.printNode(node, true, indentation + 2);
                }
            }
            if (valuesIt.hasNext()) {
                this.writer.append(newLine);
            }
        }
    }

    /**
     * Print a YAML Scalar.
     *
     * @param scalar      Given Scalar.
     * @param indentation Level of indentation of the printed Scalar.
     * @throws IOException If an I/O problem occurs.
     */
    private void printScalar(
            final Scalar scalar,
            final int indentation
    ) throws IOException {
        if (scalar instanceof BaseFoldedScalar) {
            final BaseFoldedScalar foldedScalar = (BaseFoldedScalar) scalar;
            this.writer.append(">");
            if (!scalar.comment().value().isEmpty()) {
                this.writer.append(" # ").append(scalar.comment().value());
            }
            this.writer.append(System.lineSeparator());
            final List<String> unfolded = foldedScalar.unfolded();
            for (int idx = 0; idx < unfolded.size(); idx++) {
                this.writer.append(
                        this.indent(
                                unfolded.get(idx).trim(),
                                indentation + 2
                        )
                );
                if (idx < unfolded.size() - 1) {
                    this.writer.append(System.lineSeparator());
                }
            }
        } else if (scalar instanceof RtYamlScalarBuilder.BuiltLiteralBlockScalar
                || scalar instanceof ReadLiteralBlockScalar
        ) {
            this.writer.append("|");
            if (!scalar.comment().value().isEmpty()) {
                this.writer.append(" # ").append(scalar.comment().value());
            }
            this.writer
                    .append(System.lineSeparator())
                    .append(
                            this.indent(scalar.value(), indentation + 2)
                    );
        } else {
            final Comment comment = scalar.comment();
            if (comment instanceof ScalarComment) {
                String value = scalar.value();
                if (value.contains(">") || value.contains("<")) {
                    value = "\"" + value + "\""; // 解决Flutter SDK 节点解析问题
                }
                this.writer.append(this.indent(value, 0));
                final ScalarComment scalarComment = (ScalarComment) comment;
                if (!scalarComment.inline().value().isEmpty()) {
                    this.writer.append(" # ").append(
                            scalarComment.inline().value()
                    );
                }
            }
        }
    }

    /**
     * This method should be used when printing children
     * nodes of a complex Node (mapping, scalar, stream etc).
     *
     * @param node        YAML Node to print.
     * @param onNewLine   Should the child node be printed on a new line?
     * @param indentation Indentation of the print.
     * @throws IOException If any I/O error occurs.
     */
    private void printNode(
            final YamlNode node,
            final boolean onNewLine,
            final int indentation
    ) throws IOException {
        if (node == null || node.isEmpty()) {
            if (node instanceof EmptyYamlSequence) {
                this.writer.append(" ").append("[]");
            } else if (node instanceof EmptyYamlMapping) {
                this.writer.append(" ").append("{}");
            } else {
                this.writer.append(" ").append("null");
            }
        } else {
            if (onNewLine) {
                this.writer.append(System.lineSeparator());
            } else {
                this.writer.append(" ");
            }
            if (node instanceof Scalar) {
                this.printScalar((Scalar) node, indentation);
            } else if (node instanceof YamlSequence) {
                this.printSequence((YamlSequence) node, indentation);
            } else if (node instanceof YamlMapping) {
                this.printMapping((YamlMapping) node, indentation);
            } else if (node instanceof YamlStream) {
                this.printStream((YamlStream) node, indentation);
            }
        }
    }

    /**
     * Print a comment. Make sure to split the lines if there are more
     * lines separated by NewLine and also add a '# ' in front of each
     * line.
     *
     * @param node      Node containing the Comment.
     * @param alignment Indentation.
     * @return True if a comment was printed, false otherwise.
     * @throws IOException If any I/O problem occurs.
     */
    private boolean printPossibleComment(
            final YamlNode node,
            final String alignment
    ) throws IOException {
        boolean printed = false;
        if (node != null && node.comment() != null) {
            final Comment tmpComment;
            if (node.comment() instanceof ScalarComment) {
                tmpComment = ((ScalarComment) node.comment()).above();
            } else {
                tmpComment = node.comment();
            }
            final String com = tmpComment.value();
            if (com.trim().length() != 0) {
                String[] lines = com.split(System.lineSeparator());
                for (final String line : lines) {
                    this.writer
                            .append(alignment)
                            .append("# ")
                            .append(line)
                            .append(System.lineSeparator());
                }
                printed = true;
            }
        }
        return printed;
    }

    /**
     * Indent a given String value. If the String has multiple lines,
     * they will all be indented together.
     *
     * @param value       String to indent.
     * @param indentation Indentation level.
     * @return Indented String.
     */
    private String indent(final String value, final int indentation) {
        StringBuilder alignment = new StringBuilder();
        int spaces = indentation;
        while (spaces > 0) {
            alignment.append(" ");
            spaces--;
        }
        String[] lines = value.split(System.lineSeparator());
        StringBuilder printed = new StringBuilder();
        for (int idx = 0; idx < lines.length; idx++) {
            printed.append(alignment);
            printed.append(lines[idx]);
            if (idx < lines.length - 1) {
                printed.append(System.lineSeparator());
            }
        }
        return printed.toString();
    }
}