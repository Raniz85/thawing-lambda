# Welcome to [Slidev](https://github.com/slidevjs/slidev)!

To start the slide show:

- `yarn install`
- `yarn run dev`
- visit http://localhost:3030

Edit the [slides.md](./slides.md) to see the changes.

Learn more about Slidev on [documentations](https://sli.dev/).

## Updating the Theme
The theme is included as a subtree and resides in [here](https://github.com/factor10/slidev-theme-factor10).

First, make sure that you have the theme repository added as a remote

```shell
$ git remote add theme ssh://git@github.com:factor10/slidev-theme-factor10.git
```

Then pull the latest changes:

```shell
$ git subtree pull -P theme theme main
```

This pulls the latest changes from the branch _main_ on the remote named _theme_ into the directory named _theme_
