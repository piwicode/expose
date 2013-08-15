package org.piwicode.expose;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class Expose {

    final Executor ex = Executors.newSingleThreadExecutor();
    FutureTask<Void> task;

    class SwingStat extends Stat {
        @Override
        public int incAlbum() {
            final int v = super.incAlbum();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    albumField.setText(Integer.toString(v));
                }
            });
            return v;
        }

        @Override
        public int incPhoto() {
            final int v = super.incPhoto();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    photoField.setText(Integer.toString(v));
                }
            });
            return v;
        }


        @Override
        public int incStaredPhoto() {
            final int c = super.incStaredPhoto();
            final int d = nbPhotoDeleted;
            final int a = nbPhotoCreated;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStaredPhoto(c, a, d);
                }
            });
            return c;
        }

        @Override
        public int incStaredAlbum() {
            final int c = super.incStaredAlbum();
            final int d = nbAlbumDeleted;
            final int a = nbAlbumCreated;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStaredAlbum(c, a, d);
                }
            });
            return d;
        }

        @Override
        public int incPhotoCreated() {
            final int c = nbStaredPhoto;
            final int d = nbPhotoDeleted;
            final int a = super.incPhotoCreated();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStaredPhoto(c, a, d);
                }
            });
            return a;
        }

        @Override
        public int incAlbumCreated() {
            final int c = nbStaredPhoto;
            final int d = nbPhotoDeleted;
            final int a = super.incAlbumCreated();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStaredAlbum(c, a, d);
                }
            });
            return a;
        }

        @Override
        public int incPhotoDeleted() {
            final int c = nbStaredPhoto;
            final int d = super.incPhotoDeleted();
            final int a = nbPhotoCreated;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStaredPhoto(c, a, d);
                }
            });
            return d;
        }

        @Override
        public int incAlbumDeleted() {
            final int c = nbStaredAlbum;
            final int d = super.incAlbumDeleted();
            final int a = nbAlbumCreated;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setStaredAlbum(c, a, d);
                }
            });
            return d;
        }

        @Override
        public int incAlbumToProcess() {
            final int v = super.incAlbumToProcess();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    scanProgress.setIndeterminate(false);
                    scanProgress.setValue(scanProgress.getMaximum());
                    exposeProgress.setMaximum(v);
                }
            });
            return v;
        }

        @Override
        public int incAlbumDone() {
            final int v = super.incAlbumDone();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    exposeProgress.setValue(v);
                }
            });
            return v;
        }
    }

    void setStaredAlbum(int cur, int add, int del) {
        staredAlbumField.setText(cur + " (+" + add + ") (-" + del + ")");
    }

    void setStaredPhoto(int cur, int add, int del) {
        staredPhotoField.setText(cur + " (+" + add + ") (-" + del + ")");
    }

    class WhenDone implements Runnable {

        @Override
        public void run() {
            startButton.setEnabled(true);
        }
    }

    public Expose(final Path from, final Path to) {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
                scanProgress.setIndeterminate(true);
                exposeProgress.setValue(0);
                logTextArea.setText("");
                staredAlbumField.setText("0");
                staredPhotoField.setText("0");
                albumField.setText("0");
                photoField.setText("0");
                task = new FutureTask<>(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            new Worker(new SwingStat()).expose(from,to);
                        } finally {
                            SwingUtilities.invokeLater(new WhenDone());
                        }
                        return null;
                    }
                });
                ex.execute(task);
            }
        });
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        if(args.length!=2){
            JOptionPane.showMessageDialog(null, "expose <source dir> <target dir>", "Invalid command line arguments",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        final String from=args[0];
        final String to=args[1];

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set cross-platform Java L&F (also called "Metal")
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (UnsupportedLookAndFeelException e) {
                } catch (ClassNotFoundException e) {
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e) {
                }
                JFrame frame = new JFrame("Expose");
                frame.setContentPane(new Expose(Paths.get(from),Paths.get(to)).contentPane);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setSize(400, 265);
                frame.setLocationRelativeTo(null);  // *** this will center the frame
                frame.setVisible(true);
            }
        });

    }

    private JProgressBar scanProgress;
    private JProgressBar exposeProgress;
    private JTextArea logTextArea;
    private JButton startButton;
    private JLabel staredAlbumField;
    private JLabel staredPhotoField;
    private JLabel photoField;
    private JLabel albumField;
    private JPanel contentPane;
}
