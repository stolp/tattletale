/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tattletale.reporting;

import org.jboss.tattletale.Version;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedSet;

/**
 * Dump
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 * @author Jay Balunas <jbalunas@jboss.org>
 */
public class Dump
{
   /** New line character */
   private static final String NEW_LINE = System.getProperty("line.separator");

   /**
    * Generate CSS files
    *
    * @param outputDir where the reports go
    */
   public static void generateCSS(String outputDir)
   {
      byte buffer[] = new byte[8192];
      int bytesRead;

      InputStream is = null;
      OutputStream os = null;
      try
      {
         is = Dump.class.getClassLoader().getResourceAsStream("style.css");
         os = new FileOutputStream(outputDir + "style.css");

         while ((bytesRead = is.read(buffer)) != -1)
         {
            os.write(buffer, 0, bytesRead);
         }

         os.flush();
      }
      catch (Exception e)
      {
         System.err.println("GenerateCSS: " + e.getMessage());
         e.printStackTrace(System.err);
      }
      finally
      {
         try
         {
            if (is != null)
            {
               is.close();
            }
         }
         catch (IOException ioe)
         {
            // Ignore
         }

         try
         {
            if (os != null)
            {
               os.close();
            }
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }

   /**
    * Generate index.html
    *
    * @param dependenciesReports The dependencies reports
    * @param generalReports      The general reports
    * @param archiveReports      The archive reports
    * @param customReports       The custom reports as defined by the user in jboss-tattletale.properties
    * @param outputDir           where the reports go
    */
   public static void generateIndex(SortedSet<Report> dependenciesReports,
                                    SortedSet<Report> generalReports,
                                    SortedSet<Report> archiveReports,
                                    SortedSet<Report> customReports,
                                    String outputDir)
   {
      try
      {
         FileWriter fw = new FileWriter(outputDir + "index.html");
         BufferedWriter bw = new BufferedWriter(fw, 8192);

         bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" +
                  "\"http://www.w3.org/TR/html4/loose.dtd\">" + newLine());
         bw.write("<html>" + newLine());
         bw.write("<head>" + newLine());
         bw.write("  <title>" + Version.FULL_VERSION + ": Index</title>" + newLine());
         bw.write("  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">" + newLine());
         bw.write("  <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">" + newLine());
         bw.write("</head>" + newLine());
         bw.write("<body>" + newLine());
         bw.write(newLine());

         bw.write("<h1>" + Version.FULL_VERSION + "</h1>" + newLine());

         generateReportItems(bw, dependenciesReports, "Dependencies", false);
         generateReportItems(bw, generalReports, "Reports", false);
         generateReportItems(bw, archiveReports, "Archives", true);
         generateReportItems(bw, customReports, "Custom reports", false);

         bw.write(newLine());
         bw.write("<p>" + newLine());
         bw.write("<hr>" + newLine());
         bw.write("Generated by: <a href=\"http://www.jboss.org/tattletale\">" +
                  Version.FULL_VERSION + "</a>" + newLine());
         bw.write(newLine());
         bw.write("</body>" + newLine());
         bw.write("</html>" + newLine());

         bw.flush();
         bw.close();
      }
      catch (Exception e)
      {
         System.err.println("GenerateIndex: " + e.getMessage());
         e.printStackTrace(System.err);
      }
   }

   /**
    * Simple static method to return the System property of line separator.
    *
    * @return - the line separator from System properties.
    */
   public static String newLine()
   {
      return NEW_LINE;
   }

   private static void generateReportItems(BufferedWriter bw, SortedSet<Report> reports,
                                           String heading, boolean useReportName) throws IOException
   {

      if (reports != null && reports.size() > 0)
      {
         bw.write("<h2>" + heading + "</h2>" + newLine());
         bw.write("<ul>" + newLine());

         String fileBase = "index";
         for (Report r : reports)
         {
            bw.write("<li>");
            if (useReportName)
            {
               fileBase = r.getName();
            }
            bw.write("<a href=\"" + r.getDirectory() + "/" + fileBase + ".html\">" + r.getName() + "</a> (");
            bw.write("<span");
            bw.write(" style=\"color: " + ReportStatus.getStatusColor(r.getStatus()) + ";\"");
            bw.write(">");

            bw.write(ReportSeverity.getSeverityString(r.getSeverity()));
            bw.write("</span>");
            bw.write(") (" + getIndexHtmlSize(r) + ")</li>" + newLine());
         }

         bw.write("</ul>" + newLine());
      }
   }

   private static String getIndexHtmlSize(Report r)
   {
      File indexFile = new File(r.getOutputDirectory().getAbsolutePath() + File.separator + r.getIndexName());
      return ((indexFile.length() / 1024) + 1) + "KB";
   }
}
