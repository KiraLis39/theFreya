package game.freya.controls;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/*
    когда вы хотите создать Control, который также расширяет другой класс,
    создайте пользовательский интерфейс, который расширяет интерфейс Control.
    Ваш класс может стать Control, реализовав интерфейс Control и в то же время расширив другой класс.
 */
public class MyExampleControl extends AbstractControl {

    @Override
    protected void controlUpdate(float tpf) {

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
}
