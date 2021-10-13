package com.helloworld.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.helloworld.avarar.lib.base.AvatarView;
import com.helloworld.avarar.lib.base.AvatarViewHelper;

public class MainActivity extends AppCompatActivity {
    AvatarView avatarView1;
    AvatarView avatarView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        avatarView1 = findViewById(R.id.avatarView1);
        avatarView2 = findViewById(R.id.avatarView2);

        AvatarViewHelper
                .with(this)
                .level(AvatarViewHelper.LEVEL_2)
                .networkIcon("https://img-hello-world.oss-cn-beijing.aliyuncs.com/imgs/d71032954e4d35a8602b7182223f79cc.jpg")
                .into(avatarView1);

        AvatarViewHelper
                .with(this)
                .level(AvatarViewHelper.LEVEL_3)
                .nameAndEmail("张三","1818@88.com")
                .into(avatarView2);
    }
}