package com.cesoft.encuentrame3.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.FrgMain;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.util.Constantes;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova
// A FragmentPagerAdapter that returns a fragment corresponding to one of the sections/tabs/pages.
public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private static final int MAX_PAGES = 3;
    private final FrgMain[] frmMain = new FrgMain[3];

    public SectionsPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public FrgMain getPage(int position) {
        if(position > 0 && position < MAX_PAGES)
            return frmMain[position];
        else
            return null;
    }

    @Override
    @NonNull
    public Fragment getItem(int position)
    {
        frmMain[position] = FrgMain.newInstance(position);
        return frmMain[position];
    }
    @Override
    public int getCount()
    {
        return MAX_PAGES;
    }
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case Constantes.LUGARES:return App.getInstance().getString(R.string.lugares);
            case Constantes.RUTAS:	return App.getInstance().getString(R.string.rutas);
            case Constantes.AVISOS:	return App.getInstance().getString(R.string.avisos);
            default:break;
        }
        return null;
    }
}