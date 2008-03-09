package org.objectstyle.wolips.componenteditor.inspector;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.objectstyle.wolips.baseforplugins.util.ComparisonUtils;
import org.objectstyle.wolips.bindings.wod.IWodBinding;
import org.objectstyle.wolips.bindings.wod.IWodElement;
import org.objectstyle.wolips.bindings.wod.SimpleWodBinding;
import org.objectstyle.wolips.wodclipse.core.completion.WodParserCache;
import org.objectstyle.wolips.wodclipse.core.refactoring.ChangeBindingNameRefactoring;
import org.objectstyle.wolips.wodclipse.core.refactoring.ChangeBindingValueRefactoring;

public class RefactoringWodBinding {
	public static final String BINDING_NAME = "bindingName";

	public static final String BINDING_VALUE = "bindingValue";

	private PropertyChangeSupport _propertyChange;

	private IWodElement _wodElement;

	private SimpleWodBinding _wodBinding;

	private WodParserCache _cache;

	public RefactoringWodBinding(IWodElement element, IWodBinding binding, WodParserCache cache) {
		_cache = cache;
		_wodElement = element;
		_wodBinding = new SimpleWodBinding(binding);
		_propertyChange = new PropertyChangeSupport(this);
	}

	public SimpleWodBinding getWodBinding() {
		return _wodBinding;
	}

	public void setValue(String value) throws CoreException, InvocationTargetException, InterruptedException {
		String oldValue = _wodBinding.getValue();
		String newValue = _changeValue(oldValue, value);
		_propertyChange.firePropertyChange(RefactoringWodBinding.BINDING_VALUE, oldValue, newValue);
	}

	public String _setValue(String value) throws CoreException, InvocationTargetException, InterruptedException {
		return _changeValue(null, value);
	}

	public String _changeValue(String oldValue, String value) throws CoreException, InvocationTargetException, InterruptedException {
		String newValue = value;
		if (!ComparisonUtils.equals(oldValue, newValue, true)) {
			newValue = newValue.trim();
			
			// Close missing quotes
			if (newValue.startsWith("\"") && !newValue.endsWith("\"")) {
				newValue = newValue + "\"";
			}
			
			// Make non-string literals with spaces of %'s into literals
			if (!newValue.startsWith("\"") && newValue.matches(".*[ %].*")) {
				newValue = "\"" + newValue + "\"";
			}
			
			// Process values for inline bindings
			if (_wodElement.isInline()) {
				if (newValue.startsWith("\"")) {
					newValue = newValue.substring(1, newValue.length() - 1);
				} else if (!newValue.startsWith("$")) {
					newValue = "$" + value;
				}
			}
			
			ChangeBindingValueRefactoring.run(newValue, _wodElement, _wodBinding, _cache, new NullProgressMonitor());
			_wodBinding.setValue(newValue);
		}
		return newValue;
	}

	public String getValue() {
		return _wodBinding.getValue();
	}

	public String getName() {
		return _wodBinding.getName();
	}

	public void setName(String name) throws CoreException, InvocationTargetException, InterruptedException {
		String oldName = _wodBinding.getName();
		ChangeBindingNameRefactoring.run(name, _wodElement, _wodBinding, _cache, new NullProgressMonitor());
		_wodBinding.setName(name);
		_propertyChange.firePropertyChange(RefactoringWodBinding.BINDING_NAME, oldName, name);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		_propertyChange.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		_propertyChange.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		_propertyChange.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		_propertyChange.removePropertyChangeListener(propertyName, listener);
	}
}
