/* ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0 
 *
 * Copyright (c) 2002 The ObjectStyle Group 
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne" 
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.wolips.wizards;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.objectstyle.wolips.core.logging.WOLipsLog;
import org.objectstyle.wolips.datasets.project.WOLipsJavaProject;
import org.objectstyle.wolips.datasets.resources.IWOLipsModel;
import org.objectstyle.wolips.templateengine.TemplateDefinition;
import org.objectstyle.wolips.templateengine.TemplateEngine;
/**
 * @author mnolte
 * @author uli
 */
public class WOComponentCreator implements IRunnableWithProgress {
	private String componentName;
	private boolean createBodyTag;
	private boolean createApiFile;
	private boolean createWooFile;
	private IResource parentResource;
	/**
	 * Constructor for WOComponentCreator.
	 */
	public WOComponentCreator(IResource parentResource, String componentName,
			boolean createBodyTag, boolean createApiFile, boolean createWooFile) {
		this.parentResource = parentResource;
		this.componentName = componentName;
		this.createBodyTag = createBodyTag;
		this.createApiFile = createApiFile;
		this.createWooFile = createWooFile;
	}
	/**
	 * @see WOProjectResourceCreator#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			createWOComponent(monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
	/**
	 * Method createWOComponent.
	 * 
	 * @param monitor
	 * @throws CoreException
	 * @throws InvocationTargetException
	 */
	public void createWOComponent(IProgressMonitor monitor)
			throws CoreException, InvocationTargetException {
		IFolder componentFolder = null;
		String componentJavaPath = null;
		WOLipsJavaProject wolipsJavaProject = null;
		switch (parentResource.getType()) {
			case IResource.PROJECT :
				componentFolder = ((IProject) parentResource)
						.getFolder(componentName + "."
								+ IWOLipsModel.EXT_COMPONENT);
				wolipsJavaProject = new WOLipsJavaProject(JavaCore
						.create((IProject) parentResource));
				componentJavaPath = wolipsJavaProject.getClasspathAccessor()
						.getProjectSourceFolder().getLocation().toOSString();
				break;
			case IResource.FOLDER :
				componentFolder = ((IFolder) parentResource)
						.getFolder(componentName + "."
								+ IWOLipsModel.EXT_COMPONENT);
				wolipsJavaProject = new WOLipsJavaProject(JavaCore
						.create(parentResource.getProject()));
				componentJavaPath = wolipsJavaProject.getClasspathAccessor()
						.getSubprojectSourceFolder((IFolder) parentResource,
								true).getLocation().toOSString();
				break;
			default :
				throw new InvocationTargetException(new Exception(
						"Wrong parent resource - check validation"));
		}
		componentFolder.create(false, true, monitor);
		String projectName = parentResource.getProject().getName();
		String path = componentFolder.getLocation().toOSString();
		String projectPath = parentResource.getProject().getLocation()
				.toOSString();
		TemplateEngine templateEngine = new TemplateEngine();
		try {
			templateEngine.init();
		} catch (Exception e) {
			WOLipsLog.log(e);
			throw new InvocationTargetException(e);
		}
		templateEngine.getWolipsContext().setProjectName(projectName);
		templateEngine.getWolipsContext().setCreateBodyTag(createBodyTag);
		templateEngine.getWolipsContext().setComponentName(componentName);
		templateEngine.addTemplate(new TemplateDefinition(
				"wocomponent/wocomponent.html.vm", path, componentName + "."
						+ IWOLipsModel.EXT_HTML, IWOLipsModel.EXT_HTML));
		templateEngine.addTemplate(new TemplateDefinition(
				"wocomponent/wocomponent.wod.vm", path, componentName + "."
						+ IWOLipsModel.EXT_WOD, IWOLipsModel.EXT_WOD));
		if (createWooFile)
			templateEngine.addTemplate(new TemplateDefinition(
					"wocomponent/wocomponent.woo.vm", path, componentName + "."
							+ IWOLipsModel.EXT_WOO, IWOLipsModel.EXT_WOO));
		templateEngine.addTemplate(new TemplateDefinition(
				"wocomponent/wocomponent.java.vm", componentJavaPath,
				componentName + "." + IWOLipsModel.EXT_JAVA,
				IWOLipsModel.EXT_JAVA));
		if (createApiFile)
			templateEngine.addTemplate(new TemplateDefinition(
					"wocomponent/wocomponent.api.vm", projectPath,
					componentName + "." + IWOLipsModel.EXT_API,
					IWOLipsModel.EXT_API));
		try {
			templateEngine.run(new NullProgressMonitor());
			parentResource.getProject().refreshLocal(IResource.DEPTH_INFINITE,
					monitor);
		} catch (Exception e) {
			WOLipsLog.log(e);
			throw new InvocationTargetException(e);
		}
	}
}
