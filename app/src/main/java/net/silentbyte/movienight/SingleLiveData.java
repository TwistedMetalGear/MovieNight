package net.silentbyte.movienight;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

/**
 * Variant of a LiveData which emits only when data is set. It will not emit on configuration change.
 */
public class SingleLiveData<T> extends MutableLiveData<T>
{
    private boolean pending = false;

    public void observe(LifecycleOwner owner, final Observer<T> observer)
    {
        super.observe(owner, t ->
        {
            if (pending)
            {
                pending = false;
                observer.onChanged(t);
            }
        });
    }

    public void setValue(T t)
    {
        pending = true;
        super.setValue(t);
    }

    public void call()
    {
        setValue(null);
    }
}
